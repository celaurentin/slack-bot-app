package ollama.domain

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

case class OllamaChat(
 id: String,
 title: String
)

object OllamaChat {
  given schema: Schema[OllamaChat] = DeriveSchema.gen[OllamaChat]

  given jsonCodec: JsonCodec[OllamaChat] = DeriveJsonCodec.gen[OllamaChat]
}
