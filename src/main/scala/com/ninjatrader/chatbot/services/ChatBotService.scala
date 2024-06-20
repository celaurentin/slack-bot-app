package com.ninjatrader.chatbot

package services

import com.ninjatrader.chatbot.adapters.SlackAppAdapter
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response
import com.slack.api.bolt.socket_mode.SocketModeApp
import zio.*
import zio.json.*
import domain.*
import ollama.adapters.OLlamaAdapter

trait ChatBotService {

  def app: IO[ChatBotError, Unit]

}

object ChatBotService {

  case class ChatBotServiceLive(slackAppAdapter: SlackAppAdapter, ollamaAdapter: OLlamaAdapter) extends ChatBotService {

    val postMessages = Seq(
      "Interesting question :sweat_smile:",
      "That's a great question :nerd_face:",
      "uhm it's a good question :thinking_face:",
      "I'm happy to explain! :sweat_smile:",
      "I'd be happy to answer! :sweat_smile:"
    )

    private def buildCommandResponse(
        responseMessage: String
    ): SlashCommandResponse = {
      SlashCommandResponse.builder
        .responseType("in_channel")
        .text(responseMessage)
        .build
    }

    private def tellme: ZIO[SlashCommandRequest & SlashCommandContext, ChatBotError, Response] = for {
      _   <- ZIO.unit
      req <- ZIO.service[SlashCommandRequest]
      ctx <- ZIO.service[SlashCommandContext]
      _            = ctx.ack()
      userQuestion = req.getPayload.getText
      userId       = req.getPayload.getUserId
      userName     = req.getPayload.getUserName
      channelId    = req.getPayload.getChannelId
      channelName  = req.getPayload.getChannelName

      _ <- ZIO.log(s"Question: $userQuestion, User: $userName - $userId, Channel: $channelId, Channel name: $channelName")

      nextInt                 = scala.util.Random.nextInt(postMessages.length)
      friendlyResponseMessage = s"""You've asked: "*$userQuestion*"... ${postMessages(nextInt)}"""
      _                       = ctx.respond(buildCommandResponse(friendlyResponseMessage))
      _                       = ctx.ack()
      actualAnswer            = s"<the actual answer goes here>"
      _                       = ctx.respond(buildCommandResponse(actualAnswer))
      result                  = ctx.ack()
    } yield result

    def app: IO[ChatBotError, Unit] = for {
      _ <- ZIO.unit
      _ <- slackAppAdapter.registerGlobalShortcut
      _ <- slackAppAdapter.registerViewSubmission
      _ <- slackAppAdapter.command("/tellme", tellme)
      _ <- slackAppAdapter.start
    } yield ()
  }

  def app: ZIO[ChatBotService, ChatBotError, Unit] = ZIO.serviceWithZIO(_.app)

  lazy val layer: ZLayer[SlackAppAdapter & OLlamaAdapter, Nothing, ChatBotService] = ZLayer.fromFunction {
    ChatBotServiceLive(_, _)
  }

}
