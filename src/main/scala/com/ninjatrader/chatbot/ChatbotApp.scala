package com.ninjatrader.chatbot

import com.ninjatrader.chatbot.adapters.SlackAppAdapter
import com.ninjatrader.chatbot.services.ChatBotService
import ollama.adapters.*
import ollama.domain.*
import zio.http.*
import zio.*

object ChatbotApp extends ZIOAppDefault {

  private val app = ChatBotService.app

  def run: ZIO[Any, Object, Unit] = app
    .provide(
      Client.default,
      OLlamaAdapter.live,
      SlackAppAdapter.default,
      ChatBotService.layer
    )
}
