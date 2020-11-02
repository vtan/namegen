package namegen.historical

import io.circe.Encoder
import io.circe.generic.semiauto._

final case class HistoricalNames(
  firstNameCount: Int,
  lastNameCount: Int,
  names: Seq[Seq[GeneratedName]]
)

object HistoricalNames {
  implicit val encoder: Encoder[HistoricalNames] = deriveEncoder
}
