package namegen

import namegen.common.StringPool
import namegen.historical.HistoricalNameService
import namegen.markov.MarkovNameService

import cats.effect.{ExitCode, IO, IOApp}
import io.prometheus.client.CollectorRegistry
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import scala.util.Random

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val dependencies = new Dependencies

    dependencies.router.use { router =>
      BlazeServerBuilder[IO]
        .bindHttp(8081, "0.0.0.0")
        .withHttpApp(router)
        .serve.compile.drain
        .as(ExitCode.Success)
    }
  }

  private class Dependencies {
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

    val prometheusExportService = {
      import io.prometheus.client.hotspot._
      val collectorRegistry = new CollectorRegistry()
      collectorRegistry.register(new StandardExports())
      collectorRegistry.register(new GarbageCollectorExports())
      collectorRegistry.register(new ThreadExports())
      collectorRegistry.register(new MemoryPoolsExports())
      PrometheusExportService[IO](collectorRegistry)
    }

    val router = for {
      metricsOps <- Prometheus.metricsOps[IO](prometheusExportService.collectorRegistry, "http")
      nameController = new NameController(historicalNameService, markovNameService, metricsOps)
    } yield Router(
      "/api" -> nameController.routes,
      "/" -> prometheusExportService.routes
    ).orNotFound
  }
}
