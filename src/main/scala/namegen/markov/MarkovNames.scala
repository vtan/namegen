package namegen.markov

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class MarkovNames(
  firstNameRules: Int,
  lastNameRules: Int,
  names: Seq[Seq[String]]
)

object MarkovNames {
  implicit val encoder: Encoder[MarkovNames] = deriveEncoder
}
