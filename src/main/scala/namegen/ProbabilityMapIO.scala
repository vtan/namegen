package namegen

import java.io.{BufferedWriter, FileWriter}
import scala.collection.immutable.{ArraySeq, TreeMap}
import scala.collection.mutable
import scala.io.Source

object ProbabilityMapIO {

  def writeBuckets[K](
    buckets: Iterable[(K, ProbabilityMap[String])],
    filename: String,
    buildLine: (K, String, Float) => Seq[String]
  ): Unit = {
    val writer = new BufferedWriter(new FileWriter(filename))
    buckets.foreach {
      case (key, map) =>
        map.foreachEntry { (probability, item) =>
          val line = buildLine(key, item, probability)
          writer.write(line.mkString(",") + "\n")
        }
    }
    writer.close()
  }

  def readBuckets[K](
    filename: String,
    parseLine: Seq[String] => (K, String, Float)
  ): Map[K, ProbabilityMap[String]] = {
    val bucketBuilders = mutable.Map.empty[K, mutable.Builder[(Float, String), ProbabilityMap[String]]]

    val source = Source.fromFile(filename)
    source.getLines().foreach { line =>
      val (key, item, probability) = parseLine(ArraySeq.unsafeWrapArray(line.split(",", -1)))
      val builder = bucketBuilders.getOrElseUpdate(key, TreeMap.newBuilder[Float, String])
      builder += (probability -> item)
    }
    source.close()

    Map.from(bucketBuilders.map {
      case (key, builder) => key -> builder.result()
    })
  }
}
