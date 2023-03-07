package namegen.historical

import namegen.common.{ProbabilityMap, ProbabilityMapIO, Sex, StringPool}
import namegen.importer.{FirstNameImporter, LastNameImporter}

object Dataset {

  def buildToFile(firstNameFilename: String, lastNameFilename: String): Unit = {
    {
      println("Building first names...")
      val names = FirstNameImporter.importWith("data-raw") { namesCounts =>
        val iterator = namesCounts.map {
          case (year, name) => (FirstNameKey(year / 10 * 10, name.sex), name.name, name.count)
        }
        ProbabilityMapBuilder.build(iterator)
      }
      ProbabilityMapIO.writeBuckets(names, firstNameFilename, firstNameToLine)
    }
    {
      println("Building last names...")
      val names = LastNameImporter.importWith("data-raw") { namesCounts =>
        val iterator = namesCounts.map { case (name, count) => ((), name, count) }
        ProbabilityMapBuilder.build(iterator)
      }
      ProbabilityMapIO.writeBuckets(names, lastNameFilename, lastNameToLine)
    }
    println("Done")
  }

  def loadFirstNamesFromFile(filename: String, stringPool: StringPool): Map[FirstNameKey, ProbabilityMap[String]] =
    ProbabilityMapIO.readBuckets(filename, parseFirstNameLine(stringPool))

  def loadLastNamesFromFile(filename: String, stringPool: StringPool): ProbabilityMap[String] =
    ProbabilityMapIO.readBuckets(filename, parseLastNameLine(stringPool))(())

  private def firstNameToLine(key: FirstNameKey, name: String, probability: Float): Seq[String] = {
    val sex = key.sex match {
      case Sex.Male => "M"
      case Sex.Female => "F"
    }
    Seq(key.decade.toString, sex, probability.toString, name)
  }

  private def parseFirstNameLine(stringPool: StringPool)(line: Seq[String]): (FirstNameKey, String, Float) = {
    val Seq(rawDecade, rawSex, rawProbability, name) = line
    val sex = rawSex match {
      case "M" => Sex.Male
      case "F" => Sex.Female
    }
    (FirstNameKey(rawDecade.toInt, sex), stringPool.pool(name), rawProbability.toFloat)
  }

  private def lastNameToLine(unit: Unit, name: String, probability: Float): Seq[String] = {
    val _ = unit
    Seq(probability.toString, name)
  }

  private def parseLastNameLine(stringPool: StringPool)(line: Seq[String]): (Unit, String, Float) = {
    val Seq(rawProbability, name) = line
    ((), stringPool.pool(name), rawProbability.toFloat)
  }
}
