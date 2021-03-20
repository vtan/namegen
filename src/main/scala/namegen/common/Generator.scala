package namegen.common

import scala.util.Random

trait Generator[T] {
  def generate(random: Random, transformArg: Float => Float): Option[T]

  def union(other: Generator[T]) = new Generator.Union[T](this, other)
}

object Generator {
  def empty[T]: SizedGenerator[T] = new SizedGenerator[T] {
    def generate(random: Random, transformArg: Float => Float): Option[T] = None
    def size: Int = 0
  }

  class Union[T](gen1: Generator[T], gen2: Generator[T]) extends Generator[T] {
    def generate(random: Random, transformArg: Float => Float): Option[T] =
      (if (random.nextBoolean()) gen1 else gen2).generate(random, transformArg)
  }
}

trait SizedGenerator[T] extends Generator[T] {
  def size: Int

  def union(other: SizedGenerator[T]) = new SizedGenerator.Union[T](this, other)
}

object SizedGenerator {
  class Union[T](gen1: SizedGenerator[T], gen2: SizedGenerator[T])
    extends Generator.Union[T](gen1, gen2) with SizedGenerator[T]
  {
    def size: Int = gen1.size + gen2.size
  }
}
