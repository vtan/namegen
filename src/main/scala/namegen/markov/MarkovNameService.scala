package namegen.markov

import namegen.{capitalizeName, ProbabilityMap}
import namegen.common.Sex

import scala.util.Random

class MarkovNameService(
  rulesets: Map[String, Ruleset[ProbabilityMap]],
  random: Random
) {
  private val minLengthRange = 3 to 6
  private val maxLengthRange = 6 to 10
  
  private val lastNameGenerator = new MarkovGenerator(rulesets("L"), minLengthRange, maxLengthRange)
  private val maleFirstNameGenerator = new MarkovGenerator(rulesets("M"), minLengthRange, maxLengthRange)
  private val femaleFirstNameGenerator = new MarkovGenerator(rulesets("F"), minLengthRange, maxLengthRange)

  def generateNames(sex: Option[Sex], limit: Int): MarkovNames = {
    val firstNameGenerator = sex match {
      case Some(Sex.Female) => femaleFirstNameGenerator
      case Some(Sex.Male) => maleFirstNameGenerator
      case None => femaleFirstNameGenerator.union(maleFirstNameGenerator)
    }
    val names = (1 to limit).flatMap { _ =>
      for {
        firstName <- firstNameGenerator.generate(random, transformArg = identity)
        lastName <- lastNameGenerator.generate(random, transformArg = identity)
      } yield Seq(capitalizeName(firstName.mkString), capitalizeName(lastName.mkString))
    }
    MarkovNames(
      firstNameRules = firstNameGenerator.size,
      lastNameRules = lastNameGenerator.size,
      names = names
    )
  }
}
