package slackservice

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse

import scala.concurrent.*

object SlackBotService {

  def buildCommandResponse(
    responseMessage: String
  ): Future[SlashCommandResponse] = {
    Future.successful(
      SlashCommandResponse
      .builder
      .responseType("in_channel")
      .text(responseMessage)
      .build
    )
  }

}
