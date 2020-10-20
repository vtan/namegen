package namegen

import scala.util.Random

class NameService(
  firstNameBuckets: Map[FirstNameKey, ProbabilityMap],
  lastNames: ProbabilityMap,
  random: Random
) {

  def generate(decade: Int, sex: Option[Sex], limit: Int): GeneratedNames = {
    val firstNames = {
      lazy val male = firstNameBuckets.getOrElse(FirstNameKey(decade, Sex.Male), Generator.empty)
      lazy val female = firstNameBuckets.getOrElse(FirstNameKey(decade, Sex.Female), Generator.empty)
      sex match {
        case Some(Sex.Male) => male
        case Some(Sex.Female) => female
        case None => UnionGenerator(male, female)
      }
    }
    val names = (1 to limit).flatMap { _ =>
      for {
        firstName <- firstNames.generate(random)
        lastName <- lastNames.generate(random)
      } yield Seq(firstName, lastName)
    }
    GeneratedNames(
      firstNameCount = firstNames.size,
      lastNameCount = lastNames.size,
      names = names
    )
  }
}
