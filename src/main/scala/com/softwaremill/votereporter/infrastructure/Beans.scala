package com.softwaremill.votereporter.infrastructure

import akka.actor.{Props, ActorSystem}
import com.pi4j.io.gpio.GpioFactory
import com.softwaremill.macwire.Macwire
import com.softwaremill.thegarden.lawn.shutdownables._
import com.softwaremill.votereporter.config.VoteReporterConfig
import com.softwaremill.votereporter.hardware.HardwareAdapter
import com.softwaremill.votereporter.votes.{VoteAcceptor, VoteRequestRouter, VoteReporterActor, VoteLoggerActor}

trait HardwareModule extends Macwire {

  lazy val gpioController = GpioFactory.getInstance()

}

trait Beans extends Macwire with HardwareModule
with DefaultShutdownHandlerModule with ShutdownOnJVMTermination {

  lazy val config = new VoteReporterConfig

  lazy val actorSystem = ActorSystem("vr-main") onShutdown { actorSystem =>
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

  lazy val voteReporterActor = actorSystem.actorOf(Props(classOf[VoteReporterActor], config))
  lazy val voteLoggerActor = actorSystem.actorOf(Props(classOf[VoteLoggerActor]))
  lazy val voteRequestRouter = actorSystem.actorOf(Props(classOf[VoteRequestRouter],
    voteLoggerActor, voteReporterActor))

  lazy val voteAcceptor = actorSystem.actorOf(Props(classOf[VoteAcceptor], voteRequestRouter, config))

  lazy val hardwareAdapter = new HardwareAdapter(gpioController, voteAcceptor)
}

object Beans extends Beans
