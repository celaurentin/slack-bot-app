package adapters

import com.slack.api.bolt.App
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response
import domain.*
import zio.*

//import com.github.devcdcc.ollama.

trait SlackAppAdapter {
  type Type = App
  def command(command: String)(f: => ZIO[SlashCommandRequest & SlashCommandContext, ChatBotError, App]): IO[ChatBotError, Type]
  def registerGlobalShortcut: IO[ChatBotError, Type]
  def registerViewSubmission: IO[ChatBotError, Type]
}
