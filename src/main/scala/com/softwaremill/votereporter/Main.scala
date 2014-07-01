package com.softwaremill.votereporter

import com.softwaremill.votereporter.infrastructure.Beans
import com.typesafe.scalalogging.slf4j.StrictLogging

object Main extends App with StrictLogging {

  val beans = Beans

  logger.info("Starting Vote Reporter.")

  beans.voteAcceptor

  if (runningOnRPi_?)
    beans.hardwareAdapter.run()

  private def runningOnRPi_? : Boolean = !System.getProperty("os.arch").startsWith("x86")
}
