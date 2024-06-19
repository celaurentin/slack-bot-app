package ollama
package domain

import zio.Chunk
import zio.json.*
import zio.schema.*

case class Tag(
  name: String
)
object Tag {
  given schema: Schema[Tag] = DeriveSchema.gen[Tag]
  given jsonCodec: JsonCodec[Tag] = DeriveJsonCodec.gen[Tag]
}

case class Content(
  tags: Chunk[Tag]
)

object Content {
  given schema: Schema[Content] = DeriveSchema.gen[Content]
  given jsonCodec: JsonCodec[Content] = DeriveJsonCodec.gen[Content]
}

case class OllamaDocument(
  collection_name: String,
  name: String,
  title: String,
  filename: String,
  user_id: String,
  content: Chunk[Content]
)
object OllamaDocument {
  given schema: Schema[OllamaDocument]       = DeriveSchema.gen[OllamaDocument]
  given jsonCodec: JsonCodec[OllamaDocument] = DeriveJsonCodec.gen[OllamaDocument]
}
