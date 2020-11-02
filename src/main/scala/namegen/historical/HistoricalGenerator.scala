package namegen.historical

import namegen.ProbabilityMap
import namegen.common.SizedGenerator

import scala.util.Random

class HistoricalGenerator(
  map: ProbabilityMap[String]
) extends SizedGenerator[GeneratedName] {

  def generate(random: Random): Option[GeneratedName] =
    map.maxBefore(random.nextFloat()).map((GeneratedName.apply _).tupled)

  def size: Int = map.size
}
