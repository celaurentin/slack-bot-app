package ollama

import domain.{Model, *}
import PromptRequest.*
import adapters.OLlamaAdapter
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:
  private val app =
    for {
      response <- OLlamaAdapter.auth(AuthRequest("email", "password"))
      _        <- Console.ConsoleLive.printLine(response)
    } yield ()

  def run: ZIO[Any, Object, Unit] = app.provide(Client.default, OLlamaAdapter.live)
end Main
