package namegen

import namegen.common.StringPool
import namegen.historical.HistoricalNameService
import namegen.markov.MarkovNameService

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import scala.util.Random

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val dependencies = new Dependencies

    import scala.concurrent.ExecutionContext.global
    BlazeServerBuilder[IO](global)
      .bindHttp(8081, "localhost")
      .withHttpApp(dependencies.router)
      .serve.compile.drain
      .as(ExitCode.Success)
  }

  class Dependencies {
    val (historicalFirstNames, historicalLastNames,  markovRulesets) = {
      val stringPool = new StringPool
      (
        historical.Dataset.loadFirstNamesFromFile("data/firstnames.csv", stringPool),
        historical.Dataset.loadLastNamesFromFile("data/lastnames.csv", stringPool),
        markov.Dataset.loadFromFile("data/markov.csv", stringPool)
      )
    }

    val random = new Random
    val historicalNameService = new HistoricalNameService(historicalFirstNames, historicalLastNames, random)
    val markovNameService = new MarkovNameService(markovRulesets, random)
    val nameController = new NameController(historicalNameService, markovNameService)

    val router = Router("/api" -> nameController.routes).orNotFound
  }
}
