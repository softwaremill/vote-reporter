package com.softwaremill.votereporter.votes

import akka.actor.Actor
import VoteRequest
import com.softwaremill.votereporter.common.LogStart
import com.typesafe.scalalogging.slf4j.LazyLogging


class VoteLoggerActor extends Actor with LazyLogging with LogStart {

  override def receive = {
    case voteRequest: VoteRequest =>
      logger.info(s"Vote request: $voteRequest")
  }

}
