package pl.edu.agh.akka.mas.island

import java.util.UUID

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.MigrationArena.{AgentState, CreateNewAgents}
import pl.edu.agh.akka.mas.problem.RollingDiceAgent
import pl.edu.agh.akka.mas.problem.RollingDiceAgent.RollOfTheDice

import scala.concurrent.duration._
import scala.util.Random._

/**
  * Created by novy on 09.04.16.
  */
class IslandActor(var neighbours: List[ActorSelection]) extends Actor with ActorLogging {

  private val migrationIsland = context.actorOf(MigrationArena.props(neighbours, 2))
  private val resultExchangeArena: ActorRef = context.actorOf(ResultExchangeArena.props())
  private var worker: ActorRef = null

  // todo fix it later
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception =>
        log.warning("exception occurred")
        Restart
    }


  override def preStart(): Unit = {
    // todo for now it only spawns new actors and doesn't store them anywhere
    worker = newAgent()
  }

  override def receive: Receive = {
    case msg@NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours
      migrationIsland forward msg

    case CreateNewAgents(agents) =>
      log.info(s"got information about neighbour solutions from ${sender()}, with data: $agents")
      agents foreach { agent => worker ! agent}
  }

  def newAgent(agentState: AgentState = randomAgentState()): ActorRef =
    context.actorOf(RollingDiceAgent.props(agentState, migrationIsland, resultExchangeArena))

  def randomAgentState(): AgentState = RollOfTheDice(nextInt(6) + 1)
}

object IslandActor {
  def props(neighbours: List[ActorSelection] = List()): Props =
    Props(new IslandActor(neighbours))

}

