package namegen

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.util.Random

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val (firstNames, lastNames) = NameLoader.load("firstnames.csv", "lastnames.csv")
    val nameService = new NameService(firstNames, lastNames, new Random)
    val nameController = new NameController(nameService)

    val router = Router("/api" -> nameController.routes).orNotFound
    import scala.concurrent.ExecutionContext.global
    BlazeServerBuilder[IO](global)
      .bindHttp(8081, "localhost")
      .withHttpApp(router)
      .serve.compile.drain
      .as(ExitCode.Success)
  }
}
