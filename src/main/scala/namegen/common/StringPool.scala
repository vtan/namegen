package namegen.common

import scala.collection.mutable

class StringPool {
  private val strings = mutable.Map.empty[String, String]

  def pool(name: String): String =
    strings.get(name) match {
      case Some(pooled) => pooled
      case None =>
        strings += (name -> name)
        name
    }
}
