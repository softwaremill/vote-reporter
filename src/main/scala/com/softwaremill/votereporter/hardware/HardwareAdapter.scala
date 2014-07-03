package com.softwaremill.votereporter.hardware

import java.util.concurrent.Executors

import akka.actor.ActorRef
import com.pi4j.io.gpio._
import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListenerDigital}
import com.softwaremill.votereporter.votes.PartialVoteRequest
import org.joda.time.DateTime


class HardwareAdapter(gpioController: GpioController, voteRequestRouter: ActorRef) {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent._

  import HardwareAdapter._

  def run() = {
    Future {
      registerLikeButton()
      registerDislikeButton()
      Executors.newSingleThreadExecutor().submit(new HeartbeatThread)
    }
  }

  private def registerDislikeButton() = {
    val dislikeButton = gpioController.provisionDigitalInputPin(DislikeButtonPin, PinPullResistance.PULL_UP)
    dislikeButton.addListener(new ButtonListener(positive = false))
  }

  private def registerLikeButton() = {
    val likeButton = gpioController.provisionDigitalInputPin(LikeButtonPin, PinPullResistance.PULL_DOWN)
    likeButton.addListener(new ButtonListener(positive = true))
  }

  class ButtonListener(positive: Boolean) extends GpioPinListenerDigital {

    var previousState = PinState.LOW
    var timeSinceLow = System.currentTimeMillis()

    override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent) {
      if (previousState == PinState.HIGH && event.getState == PinState.LOW) {
        timeSinceLow = System.currentTimeMillis()
      }
      else if (event.getState == PinState.HIGH && previousState == PinState.LOW && System.currentTimeMillis() - timeSinceLow > 200) {
        sendVoteRequest()
      }

      previousState = event.getState
    }

    private def sendVoteRequest() {
      voteRequestRouter ! PartialVoteRequest(positive, new DateTime())
    }

  }

  class HeartbeatThread extends Runnable {

    private val heartbeatLed = gpioController.provisionDigitalOutputPin(HeartbeatLedPin, PinState.LOW)

    override def run() = {
      while (true) {
        // pulse() is non-blocking by default - the second param makes the call blocking
        heartbeatLed.pulse(HeartbeatDuration, true)
      }
    }
  }

}

object HardwareAdapter {

  val LikeButtonPin = RaspiPin.GPIO_10
  val DislikeButtonPin = RaspiPin.GPIO_13

  val HeartbeatLedPin = RaspiPin.GPIO_11

  /* in milliseconds */
  val HeartbeatDuration = 1000L

}