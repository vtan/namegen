package namegen.common

import java.util.Arrays
import scala.collection.immutable.TreeMap
import scala.reflect.ClassTag

final case class ProbabilityMap[T] private (
  probabilities: Array[Float],
  values: Array[T]
) {
  def size: Int = probabilities.length

  def lastKey: Float = probabilities(probabilities.length - 1)

  def maxBefore(p: Float): Option[(Float, T)] = {
    val index = Arrays.binarySearch(probabilities, p) match {
      case found if found >= 0 => found
      case insertionPoint => -(insertionPoint + 1) - 1
    }
    if (index >= 0 && index < this.size) {
      Some(probabilities(index), values(index))
    } else {
      None
    }
  }
}

object ProbabilityMap {
  def fromSorted[T: ClassTag](tuples: scala.collection.Seq[(Float, T)]): ProbabilityMap[T] = {
    val probabilitiesBuilder = Array.newBuilder[Float]
    probabilitiesBuilder.sizeHint(tuples.size)

    val valuesBuilder = Array.newBuilder[T]
    valuesBuilder.sizeHint(tuples.size)

    tuples.foreach((probability, value) => {
      probabilitiesBuilder += probability
      valuesBuilder += value
    })

    new ProbabilityMap(probabilitiesBuilder.result(), valuesBuilder.result())
  }
}
