package namegen.markov

import namegen.{Multiset, ProbabilityMap}

import cats.kernel.Monoid
import scala.collection.immutable.{ArraySeq, TreeMap}

private[markov]
object RulesetBuilder {

  private val subwordLimit = 3
  private val smoothingExponent = 1.0 / 6

  def build(wordsWithCount: Iterator[(String, Int)]): Ruleset[ProbabilityMap] = {
    val rulesetsPerWord = wordsWithCount.map {
      case (word, count) =>
        val smoothedCount = Math.ceil(Math.pow(count.toDouble, smoothingExponent)).toInt
        phonemesToRule(wordToPhonemes(word.toLowerCase), smoothedCount)
    }

    import cats.implicits._
    val occurrences = Monoid.combineAll(rulesetsPerWord)
    occurrences.map {
      case (phonemes, rules) =>
        val (rulesToEnd, rulesToPhoneme) = rules.iterator.partition(_._1.isEmpty)
        val sortedRules = Seq.from(rulesToPhoneme ++ rulesToEnd)
        phonemes -> cumulateOccurences(sortedRules)
    }
  }

  private def wordToPhonemes(word: String): List[Phoneme] =
    if (word.isEmpty) {
      Nil
    } else {
      val phoneme = multiLetterPhonemes.find(word.startsWith).getOrElse(word.head.toString)
      phoneme :: wordToPhonemes(word.drop(phoneme.length))
    }

  private def phonemesToRule(phonemes: List[Phoneme], count: Int): Ruleset[Multiset] = {
    val subwords = phonemes.inits.map(_.takeRight(subwordLimit))
    val subwordRules: Iterator[Ruleset[Multiset]] = subwords.collect {
      case prefix :+ next if prefix.nonEmpty => Map(ArraySeq.from(prefix) -> Map(next -> count))
    }
    val startOfWordRule: Ruleset[Multiset] = Map(ArraySeq.empty -> Map(phonemes.head -> count))
    val endOfWordRule: Ruleset[Multiset] = Map(ArraySeq.from(phonemes.takeRight(subwordLimit - 1)) -> Map("" -> count))

    import cats.implicits._
    Monoid.combineAll(subwordRules ++ Iterable(startOfWordRule, endOfWordRule))
  }

  private def cumulateOccurences[T](occurences: Iterable[(T, Int)]): ProbabilityMap[T] = {
    val sum = occurences.map(_._2).sum.toFloat
    val (_, result) = occurences.foldLeft((0, TreeMap.empty[Float, T])) {
      case ((partialSum, result), (item, count)) =>
        val cumulatedProbability = partialSum.toFloat / sum
        (partialSum + count, result + (cumulatedProbability -> item))
    }
    result
  }

  private val multiLetterPhonemes: ArraySeq[Phoneme] = ArraySeq(
    "aw", "ai", "ay", "au", "aigh", "ae", "aa", "ah",
    "ch", "ck", "cz", "cs",
    "ew", "ea", "eigh", "ey", "ei", "eu", "eh",
    "gh", "gh",
    "hn", "hr", "hm",
    "ie", "igh", "ih",
    "ng",
    "orough", "ow", "ought", "ough", "ou", "oy", "oi", "oo", "oa", "oh",
    "ph",
    "ques", "que", "quis", "qui", "qu",
    "sh", "sch",
    "th", "th", "ts", "tz",
    "ur", "uy", "uh",
    "wh", "wr",
    "zh"
  ).sortBy(- _.length)
}
