package namegen.common

import cats.effect.{Clock, Sync}
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.metrics.MetricsOps
import org.http4s.server.middleware.Metrics

abstract class Controller[F[_]](metricsOps: MetricsOps[F]) {

  def meteredRoute(name: String)(
    pf: PartialFunction[Request[F], F[Response[F]]]
  )(implicit clock: Clock[F], sync: Sync[F]): HttpRoutes[F] =
    Metrics(metricsOps, classifierF = (_: Request[F]) => Some(name))(HttpRoutes.of(pf))
}
