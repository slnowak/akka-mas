package pl.edu.agh.akka.mas.island

import java.util.UUID

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.IslandActor.FakeAgentState
import pl.edu.agh.akka.mas.island.MigrationArena.{AgentState, CreateNewAgents}

import scala.concurrent.duration._

/**
  * Created by novy on 09.04.16.
  */
class IslandActor(var neighbours: List[ActorSelection], workers: Int) extends Actor with ActorLogging {

  private val migrationIsland = context.actorOf(MigrationArena.props(neighbours, 2))
  private val resultExchangeArena: ActorRef = context.actorOf(ResultExchangeArena.props())

  // todo fix it later
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception =>
        log.warning("exception occurred")
        Restart
    }


  override def preStart(): Unit = {
    // todo for now it only spawns new actors and doesn't store them anywhere
    0 to workers foreach { _ => newAgent() }
  }

  override def receive: Receive = {
    case msg@NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours
      migrationIsland forward msg

    case CreateNewAgents(agents) =>
      log.info(s"got request to create new workers from ${sender()}, with data: $agents")
      agents foreach newAgent
  }

  def newAgent(agentState: AgentState = randomAgentState()): ActorRef =
    context.actorOf(AgentActor.props(agentState, migrationIsland, resultExchangeArena))

  def randomAgentState(): AgentState = FakeAgentState(UUID.randomUUID().toString)
}

object IslandActor {
  def props(neighbours: List[ActorSelection] = List(), workers: Int = 10): Props =
    Props(new IslandActor(neighbours, workers))

  case class FakeAgentState(id: String) extends AgentState

}

