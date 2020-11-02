package namegen

import namegen.historical.HistoricalNameService
import namegen.markov.MarkovNameService

import cats.effect.IO
import org.http4s.{HttpDate, HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.CacheDirective._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.headers._
import scala.concurrent.duration.Duration

class NameController(
  historicalNameService: HistoricalNameService,
  markovNameService: MarkovNameService
) {
  val routes: HttpRoutes[IO] = {
    import Params._
    HttpRoutes.of[IO] {

      case GET -> Root / "names" / "historical" :? Decade(decade) +& Sex(sex) +& Limit(limit) =>
        Ok(
          historicalNameService.generateNames(decade, sex, limit.getOrElse(20)),
          noCache: _*
        )

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
}
