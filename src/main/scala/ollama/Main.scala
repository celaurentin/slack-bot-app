package ollama

import domain.{Model, *}
import PromptRequest.*
import adapters.OLlamaAdapter
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:
  private val app =
    for {
      authorization <- OLlamaAdapter.startSessionManger
      documents     <- OLlamaAdapter.getDocuments
      chats         <- OLlamaAdapter.getChats
      answers       <- OLlamaAdapter.executePrompt(buildPromptRequest(chats, "define FCM in less than 20 words", documents))
      _             <- Console.ConsoleLive.printLine(answers)
    } yield ()

  def run: ZIO[Any, Object, Unit] = app.provide(Client.default, OLlamaAdapter.live)
end Main
