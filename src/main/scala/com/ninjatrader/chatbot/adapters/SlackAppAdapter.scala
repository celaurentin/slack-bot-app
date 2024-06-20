package com.ninjatrader.chatbot

package adapters

import com.slack.api.bolt.App
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.model.view.Views.*
import com.slack.api.model.block.Blocks.*
import com.slack.api.model.block.element.BlockElements.*
import com.slack.api.model.block.composition.BlockCompositions.*
import domain.*
import zio.*

//import com.github.devcdcc.ollama.

trait SlackAppAdapter {
  type Type = App
  def command(command: String, f: => ZIO[SlashCommandRequest & SlashCommandContext, ChatBotError, Response]): IO[ChatBotError, Unit]
  def registerGlobalShortcut: IO[ChatBotError, Unit]
  def registerViewSubmission: IO[ChatBotError, Unit]
  def start: IO[ChatBotError, Unit]
}

object SlackAppAdapter {
  case class SlackAppAdapterLive(appRef: Ref[App]) extends SlackAppAdapter {

    override def command(command: String, f: => ZIO[SlashCommandRequest & SlashCommandContext, ChatBotError, Response]): IO[ChatBotError, Unit] = for {
      app <- appRef.get
      appResult <- ZIO
        .attempt(
          app.command(
            command,
            (req, ctx) => {
              Runtime.default.unsafe.run(f.mapError(e => new Exception(e.toString)).provide(ZLayer.fromFunction(() => req), ZLayer.fromFunction(() => ctx))).getOrThrow()
            }
          )
        )
        .mapError(e => ChatBotError.UnableToRegisterCommand(command))
      _ <- appRef.set(appResult)
    } yield ()

    override def registerGlobalShortcut: IO[ChatBotError, Unit] = for {
      app <- appRef.get
      appResult <- ZIO
        .attempt {
          app.globalShortcut(
            "socket-mode-shortcut",
            (req, ctx) => {
              ctx.asyncClient().viewsOpen {
                _.triggerId(req.getPayload.getTriggerId)
                  .view(
                    view(
                      _.`type`("modal")
                        .callbackId("modal-id")
                        .title(viewTitle(_.`type`("plain_text").text("New Task").emoji(true)))
                        .submit(viewSubmit(_.`type`("plain_text").text("Submit").emoji(true)))
                        .close(viewClose(_.`type`("plain_text").text("Cancel").emoji(true)))
                        .blocks(
                          asBlocks(
                            input(
                              _.blockId("input-task")
                                .element(plainTextInput(_.actionId("input").multiline(true)))
                                .label(plainText(_.text("Description")))
                            )
                          )
                        )
                    )
                  )
              }
              ctx.ack()
            }
          )
        }
        .mapError(e => ChatBotError.UnableToRegisterGlobalShortcut(e.toString))
      _ <- appRef.set(appResult)
    } yield ()

    override def registerViewSubmission: IO[ChatBotError, Unit] = for {
      app <- appRef.get
      appResult <- ZIO
        .attempt {
          app.viewSubmission(
            "modal-id",
            (req, ctx) => {
              ctx.logger.info("Submitted data: {}", req.getPayload.getView.getState.getValues)
              ctx.ack()
            }
          )
        }
        .mapError(e => ChatBotError.UnableToRegisterViewSubmission(e.toString))
      _ <- appRef.set(appResult)
    } yield ()

    override def start: IO[ChatBotError, Unit] = for {
      app <- appRef.get
      _ = new SocketModeApp(app).start()
    } yield ()
  }
  def command(command: String, f: => ZIO[SlashCommandRequest & SlashCommandContext, ChatBotError, Response]): ZIO[SlackAppAdapter, ChatBotError, Unit] =
    ZIO.serviceWithZIO(_.command(command, f))
  def registerGlobalShortcut: ZIO[SlackAppAdapter, ChatBotError, Unit] = ZIO.serviceWithZIO(_.registerGlobalShortcut)
  def registerViewSubmission: ZIO[SlackAppAdapter, ChatBotError, Unit] = ZIO.serviceWithZIO(_.registerViewSubmission)
  private lazy val live: ZLayer[Ref[App], Nothing, SlackAppAdapter]    = ZLayer.fromFunction(SlackAppAdapterLive(_))
  lazy val default: ZLayer[Any, Nothing, SlackAppAdapter]              = ZLayer.fromZIO(Ref.make(new App())) >>> live

}
