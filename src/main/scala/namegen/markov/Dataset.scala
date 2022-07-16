package namegen.markov

import namegen.ProbabilityMap
import namegen.common.{ProbabilityMapIO, Sex, StringPool}
import namegen.importer.{FirstNameImporter, LastNameImporter}

import scala.collection.immutable.ArraySeq

object Dataset {
  import Ordering.Implicits._

  def buildToFile(filename: String): Unit = {
    println("Building first names...")
    val (maleRuleset, femaleRuleset) = FirstNameImporter.importWith("data-raw") { firstNames =>
      val (maleNames, femaleNames) = firstNames
        .map(_._2)
        .partition(_.sex == Sex.Male)
      val male = RulesetBuilder.build(maleNames.map(name => name.name -> name.count))
      val female = RulesetBuilder.build(femaleNames.map(name => name.name -> name.count))
      (male, female)
    }

    println("Building last names...")
    val lastNameRuleset = LastNameImporter.importWith("data-raw")(RulesetBuilder.build)

    val buckets =
      rulesetToBuckets("M", maleRuleset) ++
      rulesetToBuckets("F", femaleRuleset) ++
      rulesetToBuckets("L", lastNameRuleset)

    ProbabilityMapIO.writeBuckets(buckets, filename, bucketToLine)
    println("Done")
  }

  def loadFromFile(filename: String, stringPool: StringPool): Map[String, Ruleset[ProbabilityMap]] = {
    val buckets = ProbabilityMapIO.readBuckets(filename, lineToBucket(stringPool))
    buckets.groupMap(key = _._1._1) {
      case ((_, phonemes), probabilities) =>
        phonemes -> probabilities
    }.map { case (k, v) => k -> Map.from(v) }
  }

  private def rulesetToBuckets(key: String, ruleset: Ruleset[ProbabilityMap]): Iterable[((String, Seq[Phoneme]), ProbabilityMap[String])] =
    ArraySeq.from(
      ruleset.iterator.map {
        case (phonemes, probabilities) => (key, phonemes) -> probabilities
      }
    ).sortBy(_._1)

  private def bucketToLine(key: (String, Seq[Phoneme]), nextPhoneme: String, probability: Float): Seq[String] =
    Seq(key._1, key._2.mkString(" "), probability.toString, nextPhoneme)

  private def lineToBucket(stringPool: StringPool)(line: Seq[String]): ((String, ArraySeq[Phoneme]), String, Float) = {
    val Seq(key, phonemes, probability, nextPhoneme) = line
    val pooledPhonemes = ArraySeq.from(phonemes.split(' ').filter(_.nonEmpty).map(stringPool.pool))
    val pooledNextPhoneme = stringPool.pool(nextPhoneme)
    (key -> pooledPhonemes, pooledNextPhoneme, probability.toFloat)
  }
}
