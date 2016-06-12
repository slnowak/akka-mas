package pl.edu.agh.akka.mas.logging

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.actor.Actor.Receive
import kamon.Kamon
import pl.edu.agh.akka.mas.logging.LoggingActor.{MigrationPerformed, MutationPerformed}

/**
  * Created by ania on 6/11/16.
  */

object LoggingActor {
  def props(): Props = Props(classOf[LoggingActor])

  trait OperationPerformed
  case class MigrationPerformed(island: ActorRef, numberOfMigrations: Int) extends OperationPerformed
  case class MutationPerformed(island: ActorRef, numberOfMutations: Int) extends OperationPerformed
}

class LoggingActor extends Actor with ActorLogging {

  val migrationsCounter = Kamon.metrics.counter("migrations-counter")
  val mutationsCounter = Kamon.metrics.counter("mutations-counter")

  var totalNumberOfMigrations = 0
  var totalNumberOfMutations = 0

  override def receive: Receive = {
    case MigrationPerformed(island, numberOfMigrations) =>
      totalNumberOfMigrations  += numberOfMigrations
      migrationsCounter.increment(numberOfMigrations)

    case MutationPerformed(island, numberOfMutations) =>
      totalNumberOfMutations += numberOfMutations
      mutationsCounter.increment(numberOfMutations)
  }

}