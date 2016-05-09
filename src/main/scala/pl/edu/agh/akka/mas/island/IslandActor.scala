package pl.edu.agh.akka.mas.island

import java.util.UUID

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.IslandActor.FakeAgentState
import pl.edu.agh.akka.mas.island.MigrationArena.{AgentState, CreateNewAgents}
import pl.edu.agh.akka.mas.island.ResultExchangeArena.NewResultArrived

import scala.concurrent.duration._

/**
  * Created by novy on 09.04.16.
  */
class IslandActor(var neighbours: List[ActorSelection], workers: Int) extends Actor with ActorLogging {

  protected val migrationIsland = context.actorOf(MigrationArena.props(neighbours, 2))
  protected val resultExchangeArena: ActorRef = context.actorOf(ResultExchangeArena.props(neighbours))
  private var problemWorkers = List[ActorRef]()
  private var systemStateInfo: Map[ActorRef,AgentState] = Map()

  // todo fix it later
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception =>
        log.warning("exception occurred")
        Restart
    }


  override def preStart(): Unit = {
    problemWorkers = for (i <- (0 to workers).toList) yield newAgent()
    context.system.scheduler.schedule(10 seconds, 10 seconds, self, "share_system_info")
  }

  override def receive: Receive = {
    case msg@NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours
      migrationIsland forward msg
      resultExchangeArena forward msg

    case CreateNewAgents(agents) =>
      log.info(s"got request to create new workers from ${sender()}, with data: $agents")
      agents foreach newAgent

    case NewResultArrived(owner, agentState) => {
      if(!systemStateInfo.contains(owner) || agentState.betterThan(systemStateInfo(owner)))
        systemStateInfo += (owner -> agentState)
      logSystemInfo
    }
    case "share_system_info" => {
      systemStateInfo.keys foreach {agent => {
        neighbours foreach {
          neighbour => neighbour ! NewResultArrived(agent, systemStateInfo(agent))
        }
      }}
    }
  }

  def logSystemInfo : Unit = {
    log.info(s"$systemStateInfo")
  }

  def newAgent(agentState: AgentState = randomAgentState()): ActorRef =
    context.actorOf(AgentActor.props(agentState, migrationIsland, resultExchangeArena))


  def randomAgentState(): AgentState = FakeAgentState(UUID.randomUUID().toString)
}

object IslandActor {
  def props(neighbours: List[ActorSelection] = List(), workers: Int = 3): Props =
    Props(new IslandActor(neighbours, workers))

  case class FakeAgentState(id: String) extends AgentState[FakeAgentState] {
    override def betterThan(another: FakeAgentState): Boolean = true
  }
}

