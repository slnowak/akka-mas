package pl.edu.agh.akka.mas.island

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.AgentActor.{ExchangeResult, JoinArena}
import pl.edu.agh.akka.mas.island.MigrationArena.AgentState
import pl.edu.agh.akka.mas.island.ResultExchangeArena.NewResultArrived

/**
  * Created by novy on 13.04.16.
  */
class ResultExchangeArena(var neighbours: List[ActorSelection]) extends Actor with ActorLogging {

//  var workers: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours

    case ExchangeResult(newResult) =>
      broadcastNewResultToAllExpect(sender(), newResult)
  }

  def broadcastNewResultToAllExpect(sender: ActorRef, newResult: AgentState): Unit = {
    (neighbours) foreach {
      _ ! NewResultArrived(sender, newResult)
    }
  }
}

object ResultExchangeArena {
  def props(neighbours: List[ActorSelection]): Props = Props(new ResultExchangeArena(neighbours))

  case class NewResultArrived(sender: ActorRef, agentState: AgentState)

}
