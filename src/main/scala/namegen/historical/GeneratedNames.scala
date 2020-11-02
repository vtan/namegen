package namegen.historical

import io.circe.Encoder
import io.circe.generic.semiauto._

final case class GeneratedNames(
  firstNameCount: Int,
  lastNameCount: Int,
  names: Seq[Seq[GeneratedName]]
)

object GeneratedNames {
  implicit val encoder: Encoder[GeneratedNames] = deriveEncoder
}
