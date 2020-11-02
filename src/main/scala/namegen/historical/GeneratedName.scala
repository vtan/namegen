package namegen.historical

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class GeneratedName(
  cumulatedProbability: Float,
  name: String
)

object GeneratedName {
  implicit val encoder: Encoder[GeneratedName] = deriveEncoder
}
