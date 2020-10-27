import scala.collection.immutable.TreeMap

package object namegen {
  type Multiset[T] = Map[T, Int]
  type ProbabilityMap[T] = TreeMap[Float, T]

  def capitalizeName(name: String): String =
    name.head.toUpper +: name.tail.toLowerCase
}
