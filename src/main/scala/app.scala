import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.model.Attachment
import com.slack.api.model.block.Blocks.{asBlocks, input}
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.plainTextInput
import com.slack.api.model.view.Views.*

import sys.process.*
import java.util
import scala.language.postfixOps

@main def runSocketModeApp(): Unit = {

  // Enable debug logging
  System.setProperty("org.slf4j.simpleLogger.log.com.slack.api", "debug")

  // env variable SLACK_APP_TOKEN is required, i.e export SLACK_APP_TOKEN=xapp...
  val app = new App()

  app.command("/hello", (req, ctx) => {
    val userMessage = req.getPayload.getText
    val responseMessage = s"""Heya ${req.getPayload.getUserName}! just echoing your words :nerd_face: "$userMessage""""

    val response = SlashCommandResponse.builder.responseType("in_channel").text(responseMessage).build

    ctx.ack(response)
  })

  // Events API
  app.message("Hello", (req, ctx) => {
    ctx.say(s"Hi <@${req.getEvent.getUser}>!")
    ctx.ack()
  })

  // Global Shortcut & Modal
  app.globalShortcut("socket-mode-shortcut", (req, ctx) => {
    ctx.asyncClient().viewsOpen {
      _.triggerId(req.getPayload.getTriggerId)
        .view(view(_.`type`("modal")
          .callbackId("modal-id")
          .title(viewTitle(_.`type`("plain_text").text("New Task").emoji(true)))
          .submit(viewSubmit(_.`type`("plain_text").text("Submit").emoji(true)))
          .close(viewClose(_.`type`("plain_text").text("Cancel").emoji(true)))
          .blocks(asBlocks(input(_.blockId("input-task")
            .element(plainTextInput(_.actionId("input").multiline(true)))
            .label(plainText(_.text("Description")))
          )))
        ))
    }
    ctx.ack()
  })
  app.viewSubmission("modal-id", (req, ctx) => {
    ctx.logger.info("Submitted data: {}", req.getPayload.getView.getState.getValues)
    ctx.ack()
  })

  // Establish a WebSocket connection and block the current thread
  // env variable SLACK_APP_TOKEN is required
  new SocketModeApp(app).start()
}
