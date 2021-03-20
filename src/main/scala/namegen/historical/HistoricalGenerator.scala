package namegen.historical

import namegen.ProbabilityMap
import namegen.common.SizedGenerator

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
