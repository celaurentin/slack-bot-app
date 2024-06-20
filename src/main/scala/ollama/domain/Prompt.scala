package ollama.domain

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

case class Prompt(
   content: String,
   role: String
 )

object Prompt {
  given schema: Schema[Prompt] = DeriveSchema.gen[Prompt]
  given jsonCodec: JsonCodec[Prompt] = DeriveJsonCodec.gen[Prompt]
}
