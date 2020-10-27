package namegen

import scala.util.Random

trait Generator {
  def generate(random: Random): Option[GeneratedName]
  def size: Int
}

object Generator {
  object empty extends Generator {
    def generate(random: Random): Option[GeneratedName] = None
    def size: Int = 0
  }

  def fromProbabilityMap(map: ProbabilityMap[String]): Generator =
    new Generator {
      def generate(random: Random): Option[GeneratedName] =
        map.maxBefore(random.nextFloat()).map((GeneratedName.apply _).tupled)

      def size: Int = map.size
    }
}

final case class UnionGenerator(gen1: Generator, gen2: Generator) extends Generator {
  def generate(random: Random): Option[GeneratedName] =
    (if (random.nextBoolean()) gen1 else gen2).generate(random)

  def size: Int = gen1.size + gen2.size
}
