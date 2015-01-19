package com.softwaremill.votereporter.votes

import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale
import javax.net.ssl.{KeyManager, SSLContext, X509TrustManager}

import akka.actor.{Actor, ActorSystem}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.softwaremill.votereporter.common.LogStart
import com.softwaremill.votereporter.config.VoteReporterConfig
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.httpx.Json4sJacksonSupport
import spray.io.ClientSSLEngineProvider

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class Retry(voteRequest: VoteRequest, retriesLeft: Int, scheduleDelay: FiniteDuration)

object Retry {

  import scala.concurrent.duration._

  val Retries = 2000

  val InitialScheduleDelay = 20.seconds
}

class VoteReporterActor(client: VoteReporterClient) extends Actor with LazyLogging with LogStart with Json4sJacksonSupport {

  import com.softwaremill.votereporter.votes.Retry.{InitialScheduleDelay, Retries}

  override implicit def json4sJacksonFormats = new DefaultFormats {
    override protected def dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  } ++ JodaTimeSerializers.all

  import context.dispatcher

  override def receive = {
    case voteRequest: VoteRequest =>
      self ! Retry(voteRequest, Retries, InitialScheduleDelay)
    case Retry(voteRequest, retriesLeft, delay) =>
      val responseFuture = client.report(voteRequest)

      responseFuture.andThen {
        case Failure(e) =>
          logger.debug(s"Failed to post $voteRequest. Retrying.")
          scheduleDelayed(voteRequest, retriesLeft, delay)
        case Success(response) =>
          logger.info(s"Posted $voteRequest.")
      }
  }

  private def scheduleDelayed(voteRequest: VoteRequest, retriesLeft: Int, scheduleDelay: FiniteDuration) = {
    if (retriesLeft > 0) {
      context.system.scheduler.scheduleOnce(scheduleDelay, self, Retry(voteRequest, retriesLeft - 1, scheduleDelay))
    } else {
      logger.warn(s"Could not post $voteRequest. Retries limit exhausted.")
    }
  }

}

trait VoteReporterClient {
  def report(voteRequest: VoteRequest): Future[HttpResponse]

  def sendHeartbeat(): Future[HttpResponse]
}

class DefaultVoteReporterClient(config: VoteReporterConfig, implicit protected val system: ActorSystem)
  extends VoteReporterClient with Json4sJacksonSupport with SslConfiguration with LazyLogging {

  import system.dispatcher

  override implicit def json4sJacksonFormats = new DefaultFormats {
    override protected def dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  } ++ JodaTimeSerializers.all

  def report(voteRequest: VoteRequest): Future[HttpResponse] = {
    post(config.voteCounterEndpoint, voteRequest)
  }

  def sendHeartbeat(): Future[HttpResponse] = {
    post(config.heartbeatsEndpoint, Map("deviceKey" -> config.deviceKey)).andThen {
      case Success(response) => logger.debug("Heartbeat sent")
      case Failure(e) => logger.error("Failed to send heartbeat", e)
    }
  }

  private def post(url: String, data: AnyRef): Future[HttpResponse] = {
    implicit val timeout: Timeout = 60.seconds

    val endpoint = new Endpoint(url)

    val pipeline: Future[SendReceive] =
      for (
        Http.HostConnectorInfo(connector, _) <-
        IO(Http) ? Http.HostConnectorSetup(endpoint.host,
          port = endpoint.port,
          sslEncryption = endpoint.sslEncryption)
      ) yield sendReceive(connector)

    val request = Post(endpoint.path, data)
    pipeline.flatMap(_.apply(request))
  }
}

class Endpoint(uriString: String) {

  private val uri = Uri(uriString)

  def sslEncryption : Boolean = uri.scheme == "https"

  def host : String = uri.authority.host.address

  def port : Int = uri.authority.port

  def path : String = uri.path.toString()
}

trait SslConfiguration {
  implicit lazy val trustfulSslContext: SSLContext = {

    object BlindFaithX509TrustManager extends X509TrustManager {
      def checkClientTrusted(chain: Array[X509Certificate], authType: String) = ()

      def checkServerTrusted(chain: Array[X509Certificate], authType: String) = ()

      def getAcceptedIssuers = Array[X509Certificate]()
    }

    val context = SSLContext.getInstance("TLS")

    context.init(Array[KeyManager](), Array(BlindFaithX509TrustManager), null)
    context
  }

  implicit lazy val sslEngineProvider: ClientSSLEngineProvider = ClientSSLEngineProvider { engine =>
    engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
    engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
    engine.setUseClientMode(true)
    engine
  }

}

