package namegen.markov

import namegen.{capitalizeName, ProbabilityMap}
import namegen.common.Sex

import scala.util.Random

class MarkovNameService(
  rulesets: Map[String, Ruleset[ProbabilityMap]],
  random: Random
) {
  private val lastNameGenerator = new MarkovGenerator(rulesets("L"), 3 to 7, 6 to 12)
  private val maleFirstNameGenerator = new MarkovGenerator(rulesets("M"), 3 to 7, 6 to 12)
  private val femaleFirstNameGenerator = new MarkovGenerator(rulesets("F"), 3 to 7, 6 to 12)

  def generateNames(sex: Option[Sex], limit: Int): Seq[Seq[String]] = {
    val firstNameGenerator = sex match {
      case Some(Sex.Female) => femaleFirstNameGenerator
      case Some(Sex.Male) => maleFirstNameGenerator
      case None => femaleFirstNameGenerator.union(maleFirstNameGenerator)
    }
    (1 to limit).flatMap { _ =>
      for {
        firstName <- firstNameGenerator.generate(random)
        lastName <- lastNameGenerator.generate(random)
      } yield Seq(capitalizeName(firstName.mkString), capitalizeName(lastName.mkString))
    }
  }
}
