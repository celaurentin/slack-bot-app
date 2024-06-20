package ollama
package domain

import zio.Chunk
import zio.json.*
import zio.schema.*


case class Content(
    tags: Option[Chunk[Tag]]
)

object Content {
  given schema: Schema[Content]       = DeriveSchema.gen[Content]
  given jsonCodec: JsonCodec[Content] = DeriveJsonCodec.gen[Content]
}

case class OllamaDocument(
    collection_name: String,
    name: String,
    title: String,
    filename: String,
    user_id: String,
    content: Content
)

object OllamaDocument {
  given schema: Schema[OllamaDocument]       = DeriveSchema.gen[OllamaDocument]
  given jsonCodec: JsonCodec[OllamaDocument] = DeriveJsonCodec.gen[OllamaDocument]
}
