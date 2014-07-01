package com.softwaremill.votereporter.common

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.LazyLogging

trait LogStart extends Actor with LazyLogging {

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    super.preStart()
    logger.debug(s"Starting ${this.getClass.getSimpleName}.")
  }

}