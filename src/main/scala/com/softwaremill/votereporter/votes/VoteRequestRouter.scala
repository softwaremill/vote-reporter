package com.softwaremill.votereporter.votes

import akka.actor.{ActorRef, Actor}
import com.softwaremill.votereporter.common.LogStart
import com.typesafe.scalalogging.slf4j.LazyLogging


class VoteRequestRouter(voteLogger: ActorRef, voteReporter: ActorRef) extends Actor with LazyLogging with LogStart {

  override def receive = {
    case voteRequest: VoteRequest =>
      voteLogger ! voteRequest
      voteReporter ! voteRequest
  }

}