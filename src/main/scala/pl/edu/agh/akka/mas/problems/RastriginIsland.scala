package pl.edu.agh.akka.mas.problems

import akka.actor.{ActorRef, ActorSelection, Props}
import pl.edu.agh.akka.mas.island.{AgentActor, IslandActor}
import pl.edu.agh.akka.mas.island.MigrationArena.AgentState

/**
  * Created by ania on 5/8/16.
  */
class RastriginIsland(neighbours: List[ActorSelection], workers: Int) extends IslandActor(neighbours, workers) {

  override def newAgent(agentState: AgentState = randomAgentState()): ActorRef =
    context.actorOf(RastriginAgent.props(agentState, migrationIsland, resultExchangeArena))

}

object RastriginIsland {
  def props(neighbours: List[ActorSelection] = List(), workers: Int = 10): Props =
    Props(new RastriginIsland(neighbours, workers))
}