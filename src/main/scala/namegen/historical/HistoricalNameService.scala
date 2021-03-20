package namegen.historical

import namegen.ProbabilityMap
import namegen.common.{Generator, Sex}

import scala.util.Random

class HistoricalNameService(
  firstNameBuckets: Map[FirstNameKey, ProbabilityMap[String]],
  lastNames: ProbabilityMap[String],
  random: Random
) {
  private val firstNameGenerators: Map[FirstNameKey, HistoricalGenerator] =
    firstNameBuckets.map { case (k, v) => k -> new HistoricalGenerator(v) }

  private val lastNameGenerator = new HistoricalGenerator(lastNames)

  def generateNames(decade: Int, sex: Option[Sex], bias: Option[Int], limit: Int): HistoricalNames = {
    val biasTransformer = bias.fold(identity[Float])(n => x => Math.pow(x.toDouble, 1 / n.toDouble).toFloat)
    val firstNameGenerator = {
      lazy val male = firstNameGenerators.getOrElse(FirstNameKey(decade, Sex.Male), Generator.empty[GeneratedName])
      lazy val female = firstNameGenerators.getOrElse(FirstNameKey(decade, Sex.Female), Generator.empty[GeneratedName])
      sex match {
        case Some(Sex.Male) => male
        case Some(Sex.Female) => female
        case None => male.union(female)
      }
    }
    val names = (1 to limit).flatMap { _ =>
      for {
        firstName <- firstNameGenerator.generate(random, biasTransformer)
        lastName <- lastNameGenerator.generate(random, biasTransformer)
      } yield Seq(firstName, lastName)
    }
    HistoricalNames(
      firstNameCount = firstNameGenerator.size,
      lastNameCount = lastNameGenerator.size,
      names = names
    )
  }
}
