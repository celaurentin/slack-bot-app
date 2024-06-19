package ollama.adapters

import domain.*

import zio.*
import zio.http.*
import zio.schema.codec.JsonCodec.*

import zio.json.*

trait OLlamaAdapter {
  def executePrompt(promptRequest: PromptRequest): IO[OLlamaError, PromptResponse]
}
object OLlamaAdapter {
  private case class OLlamaAdapterLive(client: Client) extends OLlamaAdapter {
    private val url = "http://localhost:3000/api/generate"

    def executePrompt(promptRequest: PromptRequest): IO[OLlamaError, PromptResponse] = {
      val request = Request.post(url, Body.from(promptRequest))
      for {
        response <- client.request(request).mapError(e => ConnectionOLlamaError(e.toString)).retry(Schedule.recurs(10) && Schedule.exponential(1.second))
        body     <- response.body.asString.mapError(e => DeserializationOLlamaError(e.toString))
        response <- ZIO.fromEither(body.fromJson[PromptResponse]).mapError(e => DeserializationOLlamaError(e.toString))
      } yield response
    }.provide(Scope.default)
  }

  def executePrompt(promptRequest: PromptRequest): ZIO[OLlamaAdapter, OLlamaError, PromptResponse] = ZIO.serviceWithZIO(_.executePrompt(promptRequest))

  lazy val live: ZLayer[Client, Nothing, OLlamaAdapter]   = ZLayer.fromFunction(OLlamaAdapterLive(_))
  lazy val default: ZLayer[Any, Throwable, OLlamaAdapter] = Client.default >>> ZLayer.fromFunction(OLlamaAdapterLive(_))
}
