package namegen.markov

import namegen.common.{ProbabilityMap, SizedGenerator}

import scala.collection.immutable.ArraySeq
import scala.util.Random

class MarkovGenerator(
  ruleset: Ruleset[ProbabilityMap],
  minLengthRange: Range,
  maxLengthRange: Range
) extends SizedGenerator[Seq[Phoneme]] {

  private val lookBehindLength: Int = ruleset.keys.map(_.length).max

  val size: Int = ruleset.values.map(_.size).sum

  def generate(random: Random, transformArg: Float => Float): Option[Seq[Phoneme]] = {
    val minLength = random.between(minLengthRange.start, minLengthRange.end)
    val maxLength = random.between(maxLengthRange.start, maxLengthRange.end)

    val phonemes = Seq.unfold(ArraySeq.empty[Phoneme]) { previousPhonemes =>
      if (previousPhonemes.length >= maxLength) {
        None
      } else {
        val lookBehind = previousPhonemes.takeRight(lookBehindLength)
        ruleset.get(lookBehind).flatMap { possibilities =>
          val canEnd = previousPhonemes.length >= minLength
          val key = if (canEnd) {
            transformArg(random.nextFloat())
          } else {
            // The last rule leads to ending the word
            transformArg(random.nextFloat()) * possibilities.lastKey
          }
          val chosen = possibilities.maxBefore(key).map(_._2).filter(_.nonEmpty)
          chosen.map { phoneme =>
            val acc = previousPhonemes :+ phoneme
            phoneme -> acc
          }
        }
      }
    }
    Some(phonemes).filter(_.nonEmpty)
  }
}
