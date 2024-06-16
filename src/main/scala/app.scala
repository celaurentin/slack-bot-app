import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.model.block.Blocks.{asBlocks, input}
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.plainTextInput
import com.slack.api.model.view.Views.*

import scala.language.postfixOps

@main def runSocketModeApp(): Unit = {

  System.setProperty("org.slf4j.simpleLogger.log.com.slack.api", "debug")

  val app = new App()

  app.command("/tellme", (req, ctx) => {
    ctx.ack()
    val userQuestion = req.getPayload.getText
    val userId = req.getPayload.getUserId
    val channelId = req.getPayload.getChannelId
    val channelName = req.getPayload.getChannelName
    
    val responseMessage = s"""Hi userId: $userId, you said "$userQuestion" at <#$channelId|$channelName>"""

    val response = SlashCommandResponse
      .builder
      .responseType("in_channel")
      .text(responseMessage)
      .build
    ctx.respond(response)
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

  new SocketModeApp(app).start()
}
