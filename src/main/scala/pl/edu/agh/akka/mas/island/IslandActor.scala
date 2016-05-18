package pl.edu.agh.akka.mas.island

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.AgentActor.{ExchangeResult, RastriginSolution, RequestMigration}
import pl.edu.agh.akka.mas.island.MigrationArena.{KillAgents, SpawnNewAgents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

/**
  * Created by novy on 09.04.16.
  */
class IslandActor(var neighbours: List[ActorSelection], workers: Int) extends Actor with ActorLogging {

  val migrationArena: ActorRef = createMigrationArena()
  val resultExchangeArena: ActorRef = createResultExchangeArena()
  var problemWorkers: Set[ActorRef] = initialWorkers()
  val problemSize = 100

  def random = RandomComponent.randomData

  // todo fix it later
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception =>
        log.warning("exception occurred")
        Restart
    }


  override def preStart(): Unit = {
    context.system.scheduler.schedule(10 seconds, 10 seconds, self, "share_system_info")
  }

  override def receive: Receive = {
    case msg@NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours
      migrationArena forward msg
      resultExchangeArena forward msg

    case SpawnNewAgents(initialSolution) =>
      log.info(s"got request to create new workers from ${sender()}, with data: $initialSolution")
      val newWorkers: List[ActorRef] = initialSolution map spawnAgent
       problemWorkers ++= newWorkers

    case KillAgents(agentAddresses) =>
      agentAddresses foreach killAgent
      problemWorkers --= agentAddresses

    case msg@RequestMigration =>
      migrationArena forward msg

    case msg@ExchangeResult =>
      resultExchangeArena forward msg
  }

  private def createMigrationArena(): ActorRef = context.actorOf(MigrationArena.props(neighbours, self, 2))

  private def createResultExchangeArena(): ActorRef = context.actorOf(
    ResultExchangeArena.props(neighbours, startingSolution())
  )

  private def initialWorkers(): Set[ActorRef] = {
    (for (i <- 0 to workers) yield spawnAgent()) toSet
  }

  private def killAgent(agent: ActorRef): Unit = agent ! PoisonPill

  private def spawnAgent(): ActorRef = spawnAgent(startingSolution())

  private def spawnAgent(startingSolution: RastriginSolution): ActorRef =
    context.actorOf(AgentActor.props(startingSolution, island = self))

  private def startingSolution(): RastriginSolution = {
    RastriginSolution(Array.fill(problemSize)(random.nextUniform(-50.0, 50.0)))
  }
}

object IslandActor {
  def props(neighbours: List[ActorSelection] = List(), workers: Int = 3): Props =
    Props(new IslandActor(neighbours, workers))
}


