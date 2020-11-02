package namegen.historical

import namegen.ProbabilityMap

import scala.collection.immutable.TreeMap
import scala.collection.mutable

private[historical]
object ProbabilityMapBuilder {

  def build[BucketKey](
    lines: Iterator[(BucketKey, String, Int)]
  ): Map[BucketKey, ProbabilityMap[String]] = {
    val totalCounts = mutable.Map.empty[BucketKey, Int]
    val countBuckets = mutable.Map.empty[BucketKey, mutable.ArrayBuffer[(Int, String)]]

    lines.foreach {
      case (bucketKey, name, count) =>
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
  ): ProbabilityMap[String] = {
    val totalCount = totalCounts.getOrElse(key, 0).toFloat
    val countsByName = mutable.ArrayBuffer.from(bucket.groupMapReduce(_._2)(_._1)(_ + _))
    countsByName.sortInPlaceBy(-_._2)
    var sum = 0
    val builder = TreeMap.newBuilder[Float, String]
    countsByName.foreach {
      case (name, count) =>
        val probability = sum.toFloat / totalCount
        builder += probability -> name
        sum += count
    }
    builder.result()
  }
}
