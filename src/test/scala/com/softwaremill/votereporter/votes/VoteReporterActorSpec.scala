package com.softwaremill.votereporter.votes

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import akka.actor.{PoisonPill, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, ShouldMatchers}
import spray.http.HttpResponse

import scala.concurrent.Promise

class VoteReporterActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with FlatSpecLike with BeforeAndAfterAll with ShouldMatchers {

  import scala.concurrent.duration._

  private val testVoteRequest = VoteRequest("abc", "123", positive = true, new DateTime())

  def this() = this(ActorSystem("VoteReporterActorSpec"))

  override def afterAll() {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  it should "should immediately send the vote request when received" in {
    val client = new ClientWithSuccessfulResponse
    val voteReporterActor = system.actorOf(Props(classOf[VoteReporterActor], client))
    try {
      within(2.seconds) {
        voteReporterActor ! testVoteRequest
        awaitCond(client.invoked.get())
      }
    } finally {
      voteReporterActor ! PoisonPill
    }
  }

  it should "retry if sending the message fails" in {
    val client = new ClientWithFailureResponse
    val voteReporterActor = system.actorOf(Props(classOf[VoteReporterActor], client))
    try {
      within(2.seconds) {
        voteReporterActor ! Retry(testVoteRequest, 1, 0.seconds)
        awaitCond(client.invocationCounter.get() >= 2)
      }
    } finally {
      voteReporterActor ! PoisonPill
    }
  }

}

private[votes] class ClientWithSuccessfulResponse extends VoteReporterClient {

  val invoked = new AtomicBoolean(false)

  override def report(voteRequest: VoteRequest) = {
    val response = HttpResponse(entity = "OK")
    Promise.successful {
      invoked.set(true)
      response
    }.future
  }
}

private[votes] class ClientWithFailureResponse extends VoteReporterClient {
  val invocationCounter = new AtomicInteger(0)

  override def report(voteRequest: VoteRequest) = {
    Promise.failed {
      invocationCounter.incrementAndGet()
      new RuntimeException("an error")
    }.future
  }
}
