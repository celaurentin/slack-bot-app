package ollama
package domain

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
  case llama3
}
object Model {
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

case class PromptRequest(
    model: Model,
    prompt: String,
    stream: Option[Boolean] = Some(false),
    images: Option[Chunk[String]] = None,
    format: Option[String] = None,
    options: Option[String] = None,
    system: Option[String] = None,
    template: Option[String] = None,
    context: Option[String] = None,
    raw: Option[Boolean] = None,
    keep_alive: Option[String] = None
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

}
