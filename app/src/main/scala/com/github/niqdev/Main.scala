package com.github.niqdev

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import cats.effect._
import cats.implicits.toFunctorOps
import cats.syntax.show.toShow
import com.github.niqdev.http.Http
import com.github.niqdev.model.Configurations
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.Server

import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  def start[F[_]: ConcurrentEffect: Timer]: Resource[F, Server[F]] =
    for {
      log <- Resource.liftF(Slf4jLogger.create[F])
      configurations <- Resource.liftF(Configurations.load[F])
      _ <- Resource.liftF(log.debug(s"Configurations: ${configurations.show}"))
      _ <- Http[F].client(executionContext)
      server <- Http[F].server(configurations)
    } yield server

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

  override def run(args: List[String]): IO[ExitCode] =
    start[IO].use(_ => IO.never).as(ExitCode.Success)
}
