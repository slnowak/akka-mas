package pl.edu.agh.akka.mas.logging

import akka.actor.{Actor, ActorLogging, Props}
import akka.actor.Actor.Receive
import pl.edu.agh.akka.mas.logging.LoggingActor.MigrationPerformed

/**
  * Created by ania on 6/11/16.
  */

object LoggingActor {
  def props(): Props = Props(classOf[LoggingActor])

  trait OperationPerformed
  case object MigrationPerformed extends OperationPerformed
}

class LoggingActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case MigrationPerformed =>
      log.info(s"migration performed on island ${sender}")
  }
}

