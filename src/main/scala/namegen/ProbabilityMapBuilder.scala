package namegen

import scala.collection.immutable.SortedMap
import scala.collection.mutable

class ProbabilityMapBuilder {
  private val stringPool = mutable.Map.empty[String, String]

  def build[BucketKey](
    lines: IterableOnce[String],
    processLine: String => (BucketKey, String, Int)
  ): Map[BucketKey, ProbabilityMap] = {
    val totalCounts = mutable.Map.empty[BucketKey, Int]
    val countBuckets = mutable.Map.empty[BucketKey, mutable.ArrayBuffer[(Int, String)]]

    lines.iterator.foreach { line =>
      val (bucketKey, rawName, count) = processLine(line)
      val name = poolName(rawName)
      val _ = totalCounts.updateWith(bucketKey) {
        case Some(sum) => Some(sum + count)
        case None => Some(count)
      }
      countBuckets.getOrElseUpdate(bucketKey, mutable.ArrayBuffer.empty) += (count -> name)
    }

    val buckets = countBuckets.iterator.map {
      case (key, bucket) => key -> cumulateBucket(Map.from(totalCounts), key, bucket)
    }
    Map.from(buckets)
  }

  private def cumulateBucket[BucketKey](
    totalCounts: Map[BucketKey, Int],
    key: BucketKey,
    bucket: mutable.ArrayBuffer[(Int, String)]
  ): ProbabilityMap = {
    val totalCount = totalCounts.getOrElse(key, 0).toFloat
    val countsByName = mutable.ArrayBuffer.from(bucket.groupMapReduce(_._2)(_._1)(_ + _))
    countsByName.sortInPlaceBy(-_._2)
    var sum = 0
    val builder = SortedMap.newBuilder[Float, String]
    countsByName.foreach {
      case (name, count) =>
        val probability = sum.toFloat / totalCount
        builder += probability -> name
        sum += count
    }
    ProbabilityMap(builder.result())
  }

  private def poolName(name: String): String =
    stringPool.get(name) match {
      case Some(pooled) => pooled
      case None =>
        stringPool += ((name, name))
        name
    }
}
