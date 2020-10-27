package namegen

final case class FirstName(
  name: String,
  sex: Sex,
  count: Int
)

sealed trait Sex

object Sex {
  case object Male extends Sex
  case object Female extends Sex
}
