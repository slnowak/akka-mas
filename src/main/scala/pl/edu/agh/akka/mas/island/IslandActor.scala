package pl.edu.agh.akka.mas.island

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.MigrationArena.CreateNewAgents
import pl.edu.agh.akka.mas.problems.RastriginAgent.RastriginSolution

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by novy on 09.04.16.
  */
class IslandActor(var neighbours: List[ActorSelection], workers: Int) extends Actor with ActorLogging {

  protected val migrationIsland = context.actorOf(MigrationArena.props(neighbours, 2))

  protected val resultExchangeArena: ActorRef = context.actorOf(
    ResultExchangeArena.props(neighbours, startingSolution())
  )

  private var problemWorkers = List[ActorRef]()
  private var systemStateInfo: Map[ActorRef, RastriginSolution] = Map()

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

    //    case NewResultArrived(owner, agentState) => {
    //      if(!systemStateInfo.contains(owner) || agentState.betterThan(systemStateInfo(owner)))
    //        systemStateInfo += (owner -> agentState)
    //      logSystemInfo
    //    }
    //    case "share_system_info" => {
    //      systemStateInfo.keys foreach {agent => {
    //        neighbours foreach {
    //          neighbour => neighbour ! NewResultArrived(agent, systemStateInfo(agent))
    //        }
    //      }}
    //    }
  }

  def logSystemInfo: Unit = {
    log.info(s"$systemStateInfo")
  }

  def newAgent(starting: RastriginSolution = startingSolution()): ActorRef =
    context.actorOf(AgentActor.props(starting, migrationIsland, resultExchangeArena))

  //todo hardcoded
  private def startingSolution(): RastriginSolution = RastriginSolution(0)
}

object IslandActor {
  def props(neighbours: List[ActorSelection] = List(), workers: Int = 3): Props =
    Props(new IslandActor(neighbours, workers))
}


