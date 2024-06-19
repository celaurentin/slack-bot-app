package ollama
package domain

import zio.*
import zio.json.*

/*
 * total_duration: time spent generating the response
 * load_duration: time spent in nanoseconds loading the model
 * prompt_eval_count: number of tokens in the prompt
 * prompt_eval_duration: time spent in nanoseconds evaluating the prompt
 * eval_count: number of tokens in the response
 * eval_duration: time in nanoseconds spent generating the response
 * context: an encoding of the conversation used in this response, this can be sent in the next request to keep a conversational memory
 * response: empty if the response was streamed, if not streamed, this will contain the full response
 */
@jsonMemberNames(SnakeCase)
@jsonHintNames(SnakeCase)
case class PromptResponse(
    total_duration: Long,
    load_duration: Long,
    prompt_eval_count: Option[Long],
    prompt_eval_duration: Long,
    eval_count: Long,
    eval_duration: Long,
    context: Chunk[Int],
    response: Option[String],
    done: Option[Boolean]
)
object PromptResponse {
//  given schema: Schema[PromptResponse] = DeriveSchema.gen[PromptResponse]
  given jsonCodec: JsonCodec[PromptResponse] = DeriveJsonCodec.gen[PromptResponse]
}
