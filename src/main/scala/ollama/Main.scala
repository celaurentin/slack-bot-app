package ollama

import domain.{Model, *}
import PromptRequest.*
import adapters.OLlamaAdapter
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:
  private val app =
    for {
      response <- OLlamaAdapter.executePrompt(PromptRequest(Model.llama3, "Game of life program"))
      _        <- Console.ConsoleLive.printLine(response.toString)
    } yield ()

  def run: ZIO[Any, Object, Unit] = app.provide(Client.default, OLlamaAdapter.live)
end Main
