package com.softwaremill.votereporter.config

import com.softwaremill.thegarden.lawn.config.ConfigWithDefaults
import com.typesafe.config.{ConfigFactory, Config}


trait BaseConfig extends ConfigWithDefaults {

  def rootConfig: Config

}

class VoteReporterConfig extends BaseConfig {

  override def rootConfig = ConfigFactory.load()

  lazy val deviceKey = getString("vote-reporter.device-key", "DEFAULT")

  lazy val voteCounterEndpoint = getString("vote-reporter.vote-counter-endpoint", "http://quoll.local:8080/votes")

}
