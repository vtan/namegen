import scala.collection.immutable.TreeMap

package object namegen {
  type Multiset[T] = Map[T, Int]

  def capitalizeName(name: String): String =
    name.head.toUpper +: name.tail.toLowerCase
}
