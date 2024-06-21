package ollama
package adapters

import domain.*
import ollama.domain.*
import zio.*
import zio.http.ZClient.*
import zio.http.*
import zio.http.ZClientAspect.*
import zio.http.Header.Authorization.Bearer
import zio.schema.codec.JsonCodec.*
import zio.json.*

trait OLlamaAdapter {
  def auth(authRequest: AuthRequest): IO[OLlamaError, AuthResponse]
  def getDocuments: IO[OLlamaError, Chunk[OllamaDocument]]
  def getChats: IO[OLlamaError, Chunk[OllamaChat]]
  def executePrompt(promptRequest: PromptRequest): IO[OLlamaError, PromptResponse]
  def startSessionManger: IO[OLlamaError, Unit]
}

object OLlamaAdapter {
  private case class OLlamaAdapterLive(client: Client, token: Ref[String]) extends OLlamaAdapter {
    private val url             = "http://localhost:3000"
    private val followRedirects = client @@ ZClientAspect.followRedirects(3)((resp, message) => ZIO.logInfo(message).as(resp))

    override def getDocuments: IO[OLlamaError, Chunk[OllamaDocument]] = {
      val documentsURL = s"$url/api/v1/documents"
      for {
        bearerToken <- token.get
        request      = Request.get(documentsURL).addHeader(Header.Authorization.Bearer(bearerToken))
        response    <- followRedirects.request(request).mapError(e => ConnectionOLlamaError(e.toString))
        body        <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        _           <- Console.ConsoleLive.printLine(s"getDocuments status response: ${response.status} ${response.status.code}").ignore
        response    <- ZIO.fromEither(body.fromJson[Chunk[OllamaDocument]]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)

    override def getChats: IO[OLlamaError, Chunk[OllamaChat]] = {
      val chatsURL = s"$url/api/v1/chats"
      for {
        bearerToken <- token.get
        request      = Request.get(chatsURL).addHeader(Header.Authorization.Bearer(bearerToken))
        response    <- followRedirects.request(request).mapError(e => ConnectionOLlamaError(e.toString))
        body        <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        _           <- Console.ConsoleLive.printLine(s"getChats status response: ${response.status} ${response.status.code}").ignore
        response    <- ZIO.fromEither(body.fromJson[Chunk[OllamaChat]]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)

    def auth(authRequest: AuthRequest): IO[OLlamaError, AuthResponse] = {
      val authURL = s"$url/api/v1/auths/signin"
      val request = Request.post(authURL, Body.from(authRequest))
      for {
        response <- client.request(request).mapError(e => ConnectionOLlamaError(e.toString)).retry(Schedule.recurs(10) && Schedule.exponential(1.second))
        body     <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        response <- ZIO.fromEither(body.fromJson[AuthResponse]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)

    def executePrompt(promptRequest: PromptRequest): IO[OLlamaError, PromptResponse] = {
      val promptURL = s"$url/ollama/api/chat"

      for {
        bearerToken <- token.get
        request      = Request.post(promptURL, Body.from(promptRequest)).addHeader(Header.Authorization.Bearer(bearerToken))
        response    <- client.request(request).mapError(e => ConnectionOLlamaError(e.toString))
        _           <- Console.ConsoleLive.printLine(s"executePrompt status response: ${response.status} ${response.status.code}").ignore
        _           <- Console.ConsoleLive.printLine(s"executePrompt body: ${response.body.asString}").ignore
        body        <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        response    <- ZIO.fromEither(body.fromJson[PromptResponse]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)

    // startSessionManger is aiming to request our authentication token and store it in the Ref
    // so we can use it in the other methods
    // the token should be refreshed every hour
    private def startSessionManger0: IO[OLlamaError, Unit] =
      for {
        authorization <- auth(AuthRequest("cesar.laurentin@ninjatrader.com", "Arepa123++")).retry(Schedule.spaced(1.minutes).forever)
        _             <- token.set(authorization.token)
      } yield ()
    override def startSessionManger: IO[OLlamaError, Unit] = for {
      _ <- startSessionManger0
      _ <- startSessionManger0
             .retry(Schedule.spaced(1.minutes).forever)
             .repeat(Schedule.spaced(1.hour).forever)
             .delay(1.hour)
             .fork
    } yield ()

  }

  def auth(authRequest: AuthRequest): ZIO[OLlamaAdapter, OLlamaError, AuthResponse]                = ZIO.serviceWithZIO(_.auth(authRequest))
  def getDocuments: ZIO[OLlamaAdapter, OLlamaError, Chunk[OllamaDocument]]                         = ZIO.serviceWithZIO(_.getDocuments)
  def getChats: ZIO[OLlamaAdapter, OLlamaError, Chunk[OllamaChat]]                                 = ZIO.serviceWithZIO(_.getChats)
  def executePrompt(promptRequest: PromptRequest): ZIO[OLlamaAdapter, OLlamaError, PromptResponse] =
    ZIO.serviceWithZIO(_.executePrompt(promptRequest))
  def startSessionManger: ZIO[OLlamaAdapter, OLlamaError, Unit]                                    = ZIO.serviceWithZIO(_.startSessionManger)

  lazy val live: ZLayer[Client, Nothing, OLlamaAdapter]   = ZLayer.fromZIO {
    for {
      client <- ZIO.service[Client]
      ref    <- Ref.make("")
    } yield OLlamaAdapterLive(client, ref)
  }
  lazy val default: ZLayer[Any, Throwable, OLlamaAdapter] = Client.default >>> live
}
