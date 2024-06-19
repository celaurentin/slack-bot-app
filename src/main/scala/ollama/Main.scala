package ollama

import domain.{Model, *}
import PromptRequest.*
import adapters.OLlamaAdapter
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:
  private val app =
    for {
      authorization <- OLlamaAdapter.auth(AuthRequest("cesar.laurentin@ninjatrader.com", "Arepa123++"))
      documents     <- OLlamaAdapter.getDocuments(authorization.token)
      _             <- Console.ConsoleLive.printLine(documents)
    } yield ()

  def run: ZIO[Any, Object, Unit] = app.provide(Client.default, OLlamaAdapter.live)
end Main
