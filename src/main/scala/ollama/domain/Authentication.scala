package ollama
package domain

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.*

case class AuthRequest(
    email: String,
    password: String
)
object AuthRequest {
  given schema: Schema[AuthRequest] = DeriveSchema.gen[AuthRequest]
}

case class AuthResponse(
    token: String,
    token_type: String
)
object AuthResponse {
  given schema: Schema[AuthResponse]       = DeriveSchema.gen[AuthResponse]
  given jsonCodec: JsonCodec[AuthResponse] = DeriveJsonCodec.gen[AuthResponse]
}
