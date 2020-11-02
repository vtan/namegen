package namegen.importer

import scala.io.Source

object LastNameImporter {
  private val filename = "app_c.csv"

  def importWith[T](path: String)(process: Iterator[(String, Int)] => T): T = {
    val source = Source.fromFile(s"$path/$filename")
    val result = process(source.getLines().drop(1).map(parseLine))
    source.close()
    result
  }

  private def parseLine(line: String): (String, Int) = {
    val Array(rawName, _, rawCount) = line.split(',').take(3)
    val name = namegen.capitalizeName(rawName)
    val count = rawCount.toInt
    name ->count
  }
}
