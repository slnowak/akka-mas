package pl.edu.agh.akka.mas.island

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pl.edu.agh.akka.mas.island.AgentActor.{ExchangeResult, JoinArena}
import pl.edu.agh.akka.mas.island.MigrationArena.AgentState
import pl.edu.agh.akka.mas.island.ResultExchangeArena.NewResultArrived

/**
  * Created by novy on 13.04.16.
  */
class ResultExchangeArena extends Actor with ActorLogging {

  var workers: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case JoinArena(_) =>
      workers = workers + sender()

    case ExchangeResult(newResult) =>
      broadcastNewResultToAllExpect(sender(), newResult)
  }

  def broadcastNewResultToAllExpect(sender: ActorRef, newResult: AgentState): Unit = {
    (workers - sender) foreach {
      _ ! NewResultArrived(newResult)
    }
  }
}

object ResultExchangeArena {
  def props(): Props = Props(new ResultExchangeArena())

  case class NewResultArrived(agentState: AgentState)

}
