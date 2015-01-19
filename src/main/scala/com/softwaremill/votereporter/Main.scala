package com.softwaremill.votereporter

import com.softwaremill.votereporter.infrastructure.Beans
import com.typesafe.scalalogging.slf4j.StrictLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends App with StrictLogging {

  val beans = Beans

  logger.info("Starting Vote Reporter.")

  beans.voteAcceptor

  beans.actorSystem.scheduler.schedule(Duration.Zero, Duration(beans.config.heartbeatsInterval, SECONDS)) {
    beans.voteReporterClient.sendHeartbeat()
  }

  if (runningOnRPi_?)
    beans.hardwareAdapter.run()

  private def runningOnRPi_? : Boolean = !System.getProperty("os.arch").startsWith("x86")
}
