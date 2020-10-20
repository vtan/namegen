package namegen

import scala.io.Source

object NameLoader {

  def load(firstNamesPath: String, lastNamesPath: String): (Map[FirstNameKey, ProbabilityMap], ProbabilityMap) = {
    val builder = new ProbabilityMapBuilder
    val firstNames = {
      val source = Source.fromFile(firstNamesPath)
      val result = builder.build(source.getLines(), processFirstNameLine)
      source.close()
      result
    }
    val lastNames = {
      val source = Source.fromFile(lastNamesPath)
      val result = builder.build(source.getLines(), processLastNameLine)
      source.close()
      result
    }
    (firstNames, lastNames(()))
  }

  private def processLastNameLine(line: String): (Unit, String, Int) = {
    val Array(rawName, rawCount) = line.split(",")
    val name = rawName.head +: rawName.tail.toLowerCase
    val count = rawCount.toInt
    ((), name, count)
  }

  private def processFirstNameLine(line: String): (FirstNameKey, String, Int) = {
    val Array(rawYear, name, rawSex, rawCount) = line.split(",")
    val decade = rawYear.toInt / 10
    val sex = rawSex match {
      case "F" => Sex.Female
      case "M" => Sex.Male
    }
    val count = rawCount.toInt
    (FirstNameKey(decade, sex), name, count)
  }
}
