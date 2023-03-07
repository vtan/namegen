package namegen.historical

import namegen.common.{ProbabilityMap, SizedGenerator}

import scala.util.Random

class HistoricalGenerator(
  map: ProbabilityMap[String]
) extends SizedGenerator[GeneratedName] {

  def generate(random: Random, transformArg: Float => Float): Option[GeneratedName] = {
    val arg = transformArg(random.nextFloat())
    map.maxBefore(arg).map((GeneratedName.apply _).tupled)
  }

  def size: Int = map.size
}
