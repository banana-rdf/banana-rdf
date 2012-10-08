package org.w3.banana.util

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import scala.concurrent.util._

trait AkkaDefaults {
  def defaultActorSystem = AkkaDefaults.actorSystem
  implicit def defaultFutureDispatch = AkkaDefaults.defaultFutureDispatch
}

object AkkaDefaults {
  val actorSystem = ActorSystem("banana-dispatcher")

  val defaultFutureDispatch: MessageDispatcher = actorSystem.dispatchers.lookup("banana-async")

  val DEFAULT_CONFIG = com.typesafe.config.ConfigFactory.parseString("""
akka.actor.deployment {
  /rdfstore {
    router = round-robin
    nr-of-instances = 1
  }
}

banana-dispatcher {
  executor = "fork-join-executor"
  type = "Dispatcher"
  # this makes sure that there is only one actor at a time
  fork-join-executor {
    # Min number of threads to cap factor-based parallelism number to
    parallelism-min = 1
    # Parallelism (threads) ... ceil(available processors * factor)
    parallelism-factor = 3.0
    # Max number of threads to cap factor-based parallelism number to
    parallelism-max = 1
  }
}
""")

}
