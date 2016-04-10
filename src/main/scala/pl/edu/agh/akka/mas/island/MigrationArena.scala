package pl.edu.agh.akka.mas.island

import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.AgentActor.JoinArena
import pl.edu.agh.akka.mas.island.MigrationArena.{Agent, CreateNewAgents}

import scala.util.Random

/**
  * Created by novy on 10.04.16.
  */
class MigrationArena(var neighbours: List[ActorSelection], requiredAgentsToMigrate: Int) extends Actor with ActorLogging {
  var agentsToMigrate: List[Agent] = List.empty

  override def receive: Receive = {
    case NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours

    case JoinArena(agentState) if enoughAgentsGathered() =>
      log.info(s"$self: starting migration with agents: $agentsToMigrate to random neighbour from: $neighbours")
      agentsToMigrate = Agent(agentState, sender()) :: agentsToMigrate
      randomNeighbour() foreach migrate(agentsToMigrate reverse)
      agentsToMigrate = List.empty

    case JoinArena(agentState) =>
      agentsToMigrate = Agent(agentState, sender()) :: agentsToMigrate
  }

  private def migrate(agentsToMigrate: List[Agent])(neighbour: ActorSelection): Unit = {
    neighbour ! CreateNewAgents(agentsToMigrate.map(_.agentState))
    killAgents(agentsToMigrate)
  }

  private def killAgents(agentsToMigrate: List[Agent]): Unit =
    agentsToMigrate.map(_.agentActor) foreach (_ ! PoisonPill)

  private def enoughAgentsGathered(): Boolean = agentsToMigrate.size + 1 == requiredAgentsToMigrate

  private def randomNeighbour(): Option[ActorSelection] = Random.shuffle(neighbours).headOption
}

object MigrationArena {

  def props(neighbours: List[ActorSelection], requiredAgentsToMigrate: Int = 3): Props =
    Props(new MigrationArena(neighbours, requiredAgentsToMigrate))

  trait AgentState

  case class CreateNewAgents(agentStates: List[AgentState])

  case class Agent(agentState: AgentState, agentActor: ActorRef)

}
