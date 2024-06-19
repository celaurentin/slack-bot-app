package ollama
package domain

import zio.json.*

sealed trait OLlamaError
case class BadRequestOLlamaError(message: String)      extends OLlamaError
case class DeserializationOLlamaError(message: String) extends OLlamaError
case class ConnectionOLlamaError(message: String)      extends OLlamaError

object OLlamaError {
  given jsonCodec: JsonCodec[OLlamaError] = DeriveJsonCodec.gen[OLlamaError]
}
