package namegen

import namegen.markov.MarkovGenerator

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import scala.util.Random

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val markovGenerator = {
      val stringPool = new StringPool
      val rulesets = markov.IO.load("data/markov.csv", stringPool)
      new MarkovGenerator(rulesets("F"))
    }

    val (firstNames, lastNames) = NameLoader.load("firstnames.csv", "lastnames.csv")
    val nameService = new NameService(
      firstNames.map { case (k, v) => k -> Generator.fromProbabilityMap(v) },
      Generator.fromProbabilityMap(lastNames),
      markovGenerator,
      new Random
    )
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
