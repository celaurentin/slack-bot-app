package ollama
package domain

import zio.json.*
import zio.schema.*

case class OllamaDocument(
    id: String,
    title: String,
    content: String
)
object OllamaDocument {
  given schema: Schema[OllamaDocument]       = DeriveSchema.gen[OllamaDocument]
  given jsonCodec: JsonCodec[OllamaDocument] = DeriveJsonCodec.gen[OllamaDocument]
}
