package com.ninjatrader.chatbot

package domain

sealed trait ChatBotError
object ChatBotError {
  case class UnableToRegisterGlobalShortcut(message: String) extends ChatBotError
  case class UnableToRegisterViewSubmission(message: String) extends ChatBotError
  case class UnableToRegisterCommand(message: String)        extends ChatBotError
  case class BadRequestChatBotError(message: String)         extends ChatBotError
  case class DeserializationChatBotError(message: String)    extends ChatBotError
  case class ConnectionChatBotError(message: String)         extends ChatBotError
}
