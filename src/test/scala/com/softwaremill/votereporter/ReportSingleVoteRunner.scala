package com.softwaremill.votereporter

import com.softwaremill.votereporter.infrastructure.Beans
import com.softwaremill.votereporter.votes.PartialVoteRequest
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.joda.time.DateTime

object ReportSingleVoteRunner extends App with StrictLogging {

  val beans = Beans

  logger.info("Starting app.")

  beans.voteAcceptor ! PartialVoteRequest(positive = true, new DateTime())
}
