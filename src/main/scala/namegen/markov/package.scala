package namegen

import scala.collection.immutable.ArraySeq

package object markov {
  type Phoneme = String
  type Ruleset[Container[_]] = Map[ArraySeq[Phoneme], Container[Phoneme]]
}
