package namegen.importer

import namegen.{FirstName, Sex}

import scala.io.Source

object FirstNameImporter {

  def importWith[T](path: String)(process: Iterator[(Int, FirstName)] => T): T = {
    val files = (1880 to 2019).map { year =>
      year -> Source.fromFile(s"$path/yob$year.txt")
    }
    val iterable = files.iterator.flatMap {
      case (year, source) => source.getLines().map(line => year -> parseLine(line))
    }
    val result = process(iterable)
    files.foreach(_._2.close())
    result
  }

  private def parseLine(line: String): FirstName = {
    val Array(name, rawSex, rawCount) = line.split(",")
    val sex = rawSex match {
      case "F" => Sex.Female
      case "M" => Sex.Male
    }
    val count = rawCount.toInt
    namegen.FirstName(name, sex, count)
  }
}
