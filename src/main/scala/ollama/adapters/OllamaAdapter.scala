package ollama.adapters

import domain.*
import ollama.domain.*
import zio.*
import zio.http.*
import zio.http.Header.Authorization.Bearer
import zio.schema.codec.JsonCodec.*
import zio.json.*

trait OLlamaAdapter {
  def auth(authRequest: AuthRequest): IO[OLlamaError, AuthResponse]
  def getDocuments(bearerToken: String): IO[OLlamaError, Chunk[OllamaDocument]]
  def executePrompt(promptRequest: PromptRequest): IO[OLlamaError, PromptResponse]
}

object OLlamaAdapter {
  private case class OLlamaAdapterLive(client: Client) extends OLlamaAdapter {
    private val url = "http://localhost:3000"

    override def getDocuments(bearerToken: String): IO[OLlamaError, Chunk[OllamaDocument]] = {
      val documentsURL = s"$url/api/v1/documents"
      import java.net.http.*
      import java.net.*

      val responseValue = ZIO
        .attempt {
          val client0 = java.net.http.HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()
          val request0 = HttpRequest.newBuilder
            .uri(URI.create(documentsURL))
            .setHeader("Authorization", s"Bearer $bearerToken")
            .build
          client0.send(request0, java.net.http.HttpResponse.BodyHandlers.ofString)
        }
        .mapError(e => ConnectionOLlamaError(e.toString))

//      val request = Request
//        .get(documentsURL)
//        .addHeader(Header.Host("localhost:3000"))
//        .addHeader("Location", "http://localhost:3000/api/v1/documents")
//        .addHeader("Content-Type", "application/json")
//        .addHeader("Accept", "application/json")
//        .addHeader("Connection", "keep-alive")

      for {

        response <- responseValue
        // client.request(request).mapError(e => ConnectionOLlamaError(e.toString))
        body = response.body()
//        _        <- Console.ConsoleLive.printLine(response.status).ignore
//        _        <- Console.ConsoleLive.printLine(response.status.code).ignore
//        _        <- Console.ConsoleLive.printLine(response.headers.toSeq).ignore
        response <- ZIO.fromEither(body.fromJson[Chunk[OllamaDocument]]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }

    def auth(authRequest: AuthRequest): IO[OLlamaError, AuthResponse] = {
      val authURL = s"$url/api/v1/auths/signin"
      val request = Request.post(authURL, Body.from(authRequest))
      for {
        response <- client.request(request).mapError(e => ConnectionOLlamaError(e.toString)).retry(Schedule.recurs(10) && Schedule.exponential(1.second))
        body     <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        // _        <- Console.ConsoleLive.printLine(body).ignore
        response <- ZIO.fromEither(body.fromJson[AuthResponse]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)

    def executePrompt(promptRequest: PromptRequest): IO[OLlamaError, PromptResponse] = {
      val promptURL = s"$url/api/v1/prompts" // /api/generate
      val request   = Request.post(promptURL, Body.from(promptRequest))
      for {
        response <- client.request(request).mapError(e => ConnectionOLlamaError(e.toString)).retry(Schedule.recurs(10) && Schedule.exponential(1.second))
        body     <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        response <- ZIO.fromEither(body.fromJson[PromptResponse]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)
  }

  def auth(authRequest: AuthRequest): ZIO[OLlamaAdapter, OLlamaError, AuthResponse]                = ZIO.serviceWithZIO(_.auth(authRequest))
  def getDocuments(bearerToken: String): ZIO[OLlamaAdapter, OLlamaError, Chunk[OllamaDocument]]    = ZIO.serviceWithZIO(_.getDocuments(bearerToken))
  def executePrompt(promptRequest: PromptRequest): ZIO[OLlamaAdapter, OLlamaError, PromptResponse] = ZIO.serviceWithZIO(_.executePrompt(promptRequest))

  lazy val live: ZLayer[Client, Nothing, OLlamaAdapter]   = ZLayer.fromFunction(OLlamaAdapterLive(_))
  lazy val default: ZLayer[Any, Throwable, OLlamaAdapter] = Client.default >>> ZLayer.fromFunction(OLlamaAdapterLive(_))
}
