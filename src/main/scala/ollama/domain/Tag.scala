package ollama.domain

import zio.json.*
import zio.schema.*

case class Tag(
                name: String
              )

object Tag {
  given schema: Schema[Tag] = DeriveSchema.gen[Tag]

  given jsonCodec: JsonCodec[Tag] = DeriveJsonCodec.gen[Tag]
}
