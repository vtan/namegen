package namegen

final case class FirstNameKey(
  decade: Int,
  sex: Sex
)

sealed trait Sex

object Sex {
  case object Male extends Sex
  case object Female extends Sex
}
