package namegen

import namegen.markov.MarkovGenerator

import scala.util.Random

class NameService(
  firstNameBuckets: Map[FirstNameKey, Generator],
  lastNames: Generator,
  markovGenerator: MarkovGenerator,
  random: Random
) {

  def generateRealistic(decade: Int, sex: Option[Sex], limit: Int): GeneratedNames = {
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

  def generateMarkov(limit: Int): Seq[String] =
    (1 to limit).map { _ =>
      capitalizeName(markovGenerator.generate(random, 3 to 7, 6 to 12).mkString)
    }
}
