package com.github.niqdev

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import cats.effect._
import cats.implicits.{catsSyntaxApply, toFlatMapOps, toFunctorOps}
import com.github.ghik.silencer.silent
import com.github.niqdev.http.Http
import com.github.niqdev.service.MobileCarrierService
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  def program0[F[_]: Sync](implicit E: ConcurrentEffect[F], T: Timer[F]) =
    (for {
      settings <- Resource.liftF(Settings.load[F])
      _ <- Http[F].client(executionContext)
      server <- Http[F].server(settings)
    } yield server).allocated

  @silent
  private[this] def program[F[_]: Sync](log: Logger[F])(implicit E: ConcurrentEffect[F],
                                                        T: Timer[F]): F[Unit] =
    for {
      _ <- log.info("Hello World")
      settings <- Settings.load[F]
      _ <- program0
      balances <- MobileCarrierService.retrieveBalances[F]
      _ <- log.info(s"$settings")
      _ <- log.info(balances)
    } yield ()

  /*
  // TODO move in http package
  @silent
  private[this] def server0(settings: Settings): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(settings.httpPort.value, settings.httpHost.value)
      .withHttpApp(Api.endpoints[IO])
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)

  @silent
  private[this] def server: IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(Api.endpoints[IO])
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
   */

  private[this] def error[F[_]: Sync](log: Logger[F])(e: Throwable): F[ExitCode] =
    log.error(e)("Application failed") *> Sync[F].pure(ExitCode.Error)

  private[this] def success[F[_]: Sync, T](log: Logger[F]): T => F[ExitCode] =
    s => log.info(s"Application succeeded: $s") *> Sync[F].pure(ExitCode.Success)

  /**
    *
    */
  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger
      .create[IO]
      .flatMap(log => program[IO](log).redeemWith(error[IO](log), success[IO, Unit](log)))

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] = {
    val acquire = SyncIO(Executors.newCachedThreadPool())
    val release: ExecutorService => SyncIO[Unit] = pool =>
      SyncIO {
        pool.shutdown()
        pool.awaitTermination(10, TimeUnit.SECONDS)
        ()
      }

    Resource
      .make(acquire)(release)
      .map(ExecutionContext.fromExecutorService)
  }
}
