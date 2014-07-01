package com.softwaremill.votereporter.votes

import akka.actor.{ActorRef, Actor}
import com.softwaremill.votereporter.common.LogStart
import com.softwaremill.votereporter.config.VoteReporterConfig
import com.softwaremill.votereporter.PartialVoteRequest
import com.typesafe.scalalogging.slf4j.LazyLogging

class VoteAcceptor(voteRequestRouter: ActorRef, config: VoteReporterConfig) extends Actor
with LazyLogging with LogStart {

  override def receive = {
    case req: PartialVoteRequest =>
      voteRequestRouter ! VoteRequest(genereateVoteId, config.deviceKey, req.positive, req.castedAt)
  }

  private def genereateVoteId = java.util.UUID.randomUUID.toString

}