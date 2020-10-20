package namegen

import cats.effect.IO
import org.http4s.{HttpDate, HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.CacheDirective._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.headers._

import scala.concurrent.duration.Duration

class NameController(
  nameService: NameService
) {
  val routes: HttpRoutes[IO] = {
    import Params._
    HttpRoutes.of[IO] {
      case GET -> Root / "names" :? Decade(decade) +& Sex(sex) +& Limit(limit) =>
        Ok(
          nameService.generate(decade / 10, sex, limit.getOrElse(20)),
          `Cache-Control`(`max-age`(Duration.Zero), `no-cache`(), `must-revalidate`, `proxy-revalidate`),
          Expires(HttpDate.Epoch)
        )
    }
  }

  private object Params {
    object Decade extends QueryParamDecoderMatcher[Int]("decade")
    object Sex extends OptionalQueryParamDecoderMatcher[Sex]("sex")
    object Limit extends OptionalQueryParamDecoderMatcher[Int]("limit")(limitDeocder)
  }

  private implicit val sexDecoder: QueryParamDecoder[Sex] =
    QueryParamDecoder[String].emap {
      case "male" => Right(Sex.Male)
      case "female" => Right(Sex.Female)
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
