package ollama.domain

import ollama.domain.Model.`llama3:8b`
import zio.Chunk
import zio.json.*
import zio.schema.*

/*
 model: (required) the model name
 prompt: the prompt to generate a response for
 images: (optional) a list of base64-encoded images (for multimodal models such as llava)
 Advanced parameters (optional):

 format: the format to return a response in. Currently the only accepted value is json
 options: additional model parameters listed in the documentation for the Modelfile such as temperature
 system: system message to (overrides what is defined in the Modelfile)
 template: the prompt template to use (overrides what is defined in the Modelfile)
 context: the context parameter returned from a previous request to /generate, this can be used to keep a short conversational memory
 stream: if false the response will be returned as a single response object, rather than a stream of objects
 raw: if true no formatting will be applied to the prompt. You may choose to use the raw parameter if you are specifying a full templated prompt in your request to the API
 keep_alive: controls how long the model will stay loaded into memory following the request (default: 5m)
 */
@jsonHintNames(SnakeCase)
enum Model {
  case `llama3:8b`
}
object Model {
  given schema: Schema[Model] = DeriveSchema.gen[Model]
  given jsonCodec: JsonCodec[Model] = DeriveJsonCodec.gen[Model]
}
//sealed trait Model
//object Model {
//  case object Llama3 extends Model {}
//  given jsonCodec: JsonCodec[Model] = DeriveJsonCodec.gen[Model]
//}

//@jsonDiscriminator("model")
//@jsonMemberNames(SnakeCase)
//@jsonHintNames(SnakeCase)
//sealed trait PromptRequest {
////  def model: Model
//  def prompt: String
//  def stream: Option[Boolean]
//  def images: Option[Chunk[String]]
//  def format: Option[String]
//  def options: Option[String]
//  def system: Option[String]
//  def template: Option[String]
//  def context: Option[String]
//  def raw: Option[Boolean]
//  def keepAlive: Option[String]
//}

case class Doc(
   collection_name: String,
   content: Chunk[Tag],
   filename: String,
   name: String,
   title: String,
   `type`: String,
   user_id: String
)

object Doc {
  implicit val schema: Schema[Doc] = DeriveSchema.gen[Doc]

}

case class PromptRequest(
    model: Model,
    stream: Option[Boolean] = Some(false),
    images: Option[Chunk[String]] = None,
    format: Option[String] = None,
    options: Option[String] = None,
    system: Option[String] = None,
    template: Option[String] = None,
    context: Option[String] = None,
    raw: Option[Boolean] = None,
    keep_alive: Option[String] = None,
    chat_id: String,
    citations: Option[Boolean] = Some(false),
    messages: Chunk[Prompt],
    docs: Chunk[Doc]
)

object PromptRequest {
  @jsonMemberNames(SnakeCase)
  @jsonHintNames(SnakeCase)
  @jsonHint("llama3")
//  case class Llama3(
//      prompt: String,
//      stream: Option[Boolean],
//      images: Option[Chunk[String]],
//      format: Option[String],
//      options: Option[String],
//      system: Option[String],
//      template: Option[String],
//      context: Option[String],
//      raw: Option[Boolean],
//      keepAlive: Option[String]
//  ) extends PromptRequest
  implicit val schema: Schema[PromptRequest] = DeriveSchema.gen[PromptRequest]

  def buildMock = PromptRequest(
    chat_id = "09cc2909-7f43-4f83-8d82-20f63027e07e",
    docs = Chunk(
            Doc(
              collection_name = "e87e6929a7c4ed6633c596fda5bfc83da27b97df82b7b97332f4cb7d040be74",
              content = Chunk(Tag("Trading"), Tag("Futures"), Tag("Abbreviation")),
              filename = "Glossary.docx",
              name = "glossarydocx",
              title = "Glossary.docx",
              `type` = "doc",
              user_id = "761229f2-e5d1-4e7d-bd52-b10535578e4f"
            )),
    messages = 
          Chunk(
            Prompt(
              content = "what does FCM mean in trading?",
              role = "user"
            ),
            Prompt(
              content = "In trading, FCM stands for Futures Commissions Merchant. A Futures Commissions Merchant (FCM) is a firm that solicits or accepts orders for futures or options contracts traded on an exchange and accepts money (or securities or property) from a customer to margin, guarantee or secure the option or futures transaction.",
              role = "assistant"
            ),
            Prompt(
              content = "what's a Speculator in trading",
              role = "user"
            ),
            Prompt(
              content = "A Speculator is a trader who takes on risk by buying or selling futures contracts with the goal of making a profit. They don't actually use the underlying asset, such as commodities or currencies, but instead, they trade based on market fluctuations.",
              role = "assistant"
            )
          ),
    model = `llama3:8b`
  )
}
