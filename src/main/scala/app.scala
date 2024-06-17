import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.model.block.Blocks.{asBlocks, input}
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.plainTextInput
import com.slack.api.model.view.Views.*
import org.slf4j.LoggerFactory
import service.SlackBotService.buildCommandResponse

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.language.postfixOps
import scala.util.Random

@main def runSocketModeApp(): Unit = {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  // System.setProperty("org.slf4j.simpleLogger.log.com.slack.api", "debug")
  val LOGGER = LoggerFactory.getLogger(classOf[Nothing])
  val postMessages= Seq("Interesting question :sweat_smile:", "That's a great question :nerd_face:",
    "uhm it's a good question :thinking_face:", "I'm happy to explain! :sweat_smile:",
    "I'd be happy to answer! :sweat_smile:")
  val app = new App()

  app.command("/tellme", (req, ctx) => {
    ctx.ack()
    val userQuestion = req.getPayload.getText
    val userId = req.getPayload.getUserId
    val channelId = req.getPayload.getChannelId
    val channelName = req.getPayload.getChannelName

    LOGGER.info(s"Question: $userQuestion, User: $userId, Channel: $channelId, Channel name: $channelName")

    val friendlyResponseMessage = s"""You've asked: "*$userQuestion*"... ${postMessages(Random.nextInt(postMessages.length))}"""

    buildCommandResponse(friendlyResponseMessage).map(r => ctx.respond(r))
    ctx.ack()

    // Wrap the Ollama3 API into a Future to get the actual answer.
    val actualAnswer = s"<the actual answer goes here>"
    buildCommandResponse(actualAnswer).map(r => ctx.respond(r))
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
