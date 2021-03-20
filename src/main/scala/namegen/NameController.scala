package namegen

import namegen.common.Controller
import namegen.historical.HistoricalNameService
import namegen.markov.MarkovNameService

import cats.effect.{Clock, IO}
import cats.implicits._
import org.http4s.{HttpDate, HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.CacheDirective._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.metrics.MetricsOps
import scala.concurrent.duration.Duration

class NameController(
  historicalNameService: HistoricalNameService,
  markovNameService: MarkovNameService,
  metricsOps: MetricsOps[IO]
) extends Controller[IO](metricsOps) {

  def routes(implicit clock: Clock[IO]): HttpRoutes[IO] = {
    import Params._
    meteredRoute("names_historical") {
      case GET -> Root / "names" / "historical" :? Decade(decade) +& Sex(sex) +& Bias(bias) +& Limit(limit) =>
        Ok(
          historicalNameService.generateNames(decade, sex, bias.filter(_ > 1), limit.getOrElse(20)),
          noCache: _*
        )
    } <+> meteredRoute("names_markov") {
      case GET -> Root / "names" / "markov" :? Sex(sex) +& Limit(limit) =>
        Ok(
          markovNameService.generateNames(sex, limit.getOrElse(20)),
          noCache: _*
        )
    }
  }

  private val noCache = Seq(
    `Cache-Control`(`max-age`(Duration.Zero), `no-cache`(), `must-revalidate`, `proxy-revalidate`),
    Expires(HttpDate.Epoch)
  )

  private object Params {
    object Decade extends QueryParamDecoderMatcher[Int]("decade")
    object Sex extends OptionalQueryParamDecoderMatcher[common.Sex]("sex")
    object Limit extends OptionalQueryParamDecoderMatcher[Int]("limit")(limitDeocder)
    object Bias extends OptionalQueryParamDecoderMatcher[Int]("bias")(biasDecoder)
  }

  private implicit val sexDecoder: QueryParamDecoder[common.Sex] =
    QueryParamDecoder[String].emap {
      case "male" => Right(common.Sex.Male)
      case "female" => Right(common.Sex.Female)
      case invalid =>
        Left(ParseFailure(
          sanitized = "Invalid sex",
          details = s"Invalid sex: $invalid"
        ))
    }
  private val limitDeocder: QueryParamDecoder[Int] =
    QueryParamDecoder.intQueryParamDecoder.emap {
      case limit if (1 to 50).contains(limit) => Right(limit)
      case invalid =>
        Left(ParseFailure(
          sanitized = "Invalid limit",
          details = s"Invalid limit: $invalid"
        ))
    }
  private val biasDecoder: QueryParamDecoder[Int] =
    QueryParamDecoder.intQueryParamDecoder.emap {
      case bias if (1 to 10).contains(bias) => Right(bias)
      case invalid =>
        Left(ParseFailure(
          sanitized = "Invalid bias",
          details = s"Invalid bias: $invalid"
        ))
    }
}
