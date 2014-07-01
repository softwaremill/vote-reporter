package com.softwaremill.votereporter.hardware

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
    }
  }

  private def registerDislikeButton() = {
    val dislikeButton = gpioController.provisionDigitalInputPin(DislikeButtonPin, PinPullResistance.PULL_DOWN)
    val dislikeButtonLight = gpioController.provisionDigitalOutputPin(DislikeButtonLightPin, "dislikeButtonLight", PinState.LOW)
    dislikeButton.addListener(new ButtonListener(dislikeButtonLight, positive = false))
  }

  private def registerLikeButton() = {
    val likeButton = gpioController.provisionDigitalInputPin(LikeButtonPin, PinPullResistance.PULL_DOWN)
    val likeButtonLight = gpioController.provisionDigitalOutputPin(LikeButtonLightPin, "likeButtonLight", PinState.LOW)
    likeButton.addListener(new ButtonListener(likeButtonLight, positive = true))
  }

  class ButtonListener(val light: GpioPinDigitalOutput, positive: Boolean) extends GpioPinListenerDigital {

    override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent) {
      if (event.getState == PinState.HIGH) {
        sendVoteRequest()
        blinkLight()
      }
    }

    private def sendVoteRequest() {
      voteRequestRouter ! PartialVoteRequest(positive, new DateTime())
    }

    private def blinkLight() {
      light.blink(BlinkDelay, BlinkDuration)
    }

  }


}

object HardwareAdapter {

  val LikeButtonPin = RaspiPin.GPIO_10
  val LikeButtonLightPin = RaspiPin.GPIO_11

  val DislikeButtonPin = RaspiPin.GPIO_13
  val DislikeButtonLightPin = RaspiPin.GPIO_14

  /* in milliseconds */
  val BlinkDelay = 100L
  val BlinkDuration = 500L

}