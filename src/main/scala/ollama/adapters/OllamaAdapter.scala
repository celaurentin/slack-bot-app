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
  def getDocuments(bearerToken: String): IO[OLlamaError, Chunk[OllamaDocument]]
  def getChats(bearerToken: String): IO[OLlamaError, Chunk[OllamaChat]]
  def executePrompt(bearerToken: String, promptRequest: PromptRequest): IO[OLlamaError, PromptResponse]
}

object OLlamaAdapter {
  private case class OLlamaAdapterLive(client: Client) extends OLlamaAdapter {
    private val url             = "http://localhost:3000"
    private val followRedirects = client @@ ZClientAspect.followRedirects(3)((resp, message) => ZIO.logInfo(message).as(resp))

    override def getDocuments(bearerToken: String): IO[OLlamaError, Chunk[OllamaDocument]] = {
      val documentsURL = s"$url/api/v1/documents"
      val request = Request
        .get(documentsURL)
        .addHeader(Header.Authorization.Bearer(bearerToken))
      for {

        response <- followRedirects.request(request).mapError(e => ConnectionOLlamaError(e.toString))
        body     <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        _        <- Console.ConsoleLive.printLine(s"getDocuments status response: ${response.status} ${response.status.code}").ignore
        response <- ZIO.fromEither(body.fromJson[Chunk[OllamaDocument]]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)

    override def getChats(bearerToken: String): IO[OLlamaError, Chunk[OllamaChat]] = {
      val chatsURL = s"$url/api/v1/chats"
      val request = Request
        .get(chatsURL)
        .addHeader(Header.Authorization.Bearer(bearerToken))
      for {

        response <- followRedirects.request(request).mapError(e => ConnectionOLlamaError(e.toString))
        body <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        _ <- Console.ConsoleLive.printLine(s"getChats status response: ${response.status} ${response.status.code}").ignore
        response <- ZIO.fromEither(body.fromJson[Chunk[OllamaChat]]).mapError(e => DeserializationOLlamaError(e.toString))
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

    def executePrompt(bearerToken: String, promptRequest: PromptRequest): IO[OLlamaError, PromptResponse] = {
      val promptURL = s"$url/ollama/api/chat"
      val request   = Request
        .post(promptURL, Body.from(promptRequest))
        .addHeader(Header.Authorization.Bearer(bearerToken))
      for {
        response <- client.request(request).mapError(e => ConnectionOLlamaError(e.toString))
        _        <- Console.ConsoleLive.printLine(s"executePrompt status response: ${response.status} ${response.status.code}").ignore
        _        <- Console.ConsoleLive.printLine(s"executePrompt body: ${response.body.asString}").ignore
        body     <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        response <- ZIO.fromEither(body.fromJson[PromptResponse]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)
  }

  def auth(authRequest: AuthRequest): ZIO[OLlamaAdapter, OLlamaError, AuthResponse]                = ZIO.serviceWithZIO(_.auth(authRequest))
  def getDocuments(bearerToken: String): ZIO[OLlamaAdapter, OLlamaError, Chunk[OllamaDocument]]    = ZIO.serviceWithZIO(_.getDocuments(bearerToken))
  def getChats(bearerToken: String): ZIO[OLlamaAdapter, OLlamaError, Chunk[OllamaChat]]    = ZIO.serviceWithZIO(_.getChats(bearerToken))
  def executePrompt(bearerToken: String, promptRequest: PromptRequest): ZIO[OLlamaAdapter, OLlamaError, PromptResponse] = ZIO.serviceWithZIO(_.executePrompt(bearerToken, promptRequest))

  lazy val live: ZLayer[Client, Nothing, OLlamaAdapter]   = ZLayer.fromFunction(OLlamaAdapterLive(_))
  lazy val default: ZLayer[Any, Throwable, OLlamaAdapter] = Client.default >>> ZLayer.fromFunction(OLlamaAdapterLive(_))
}
