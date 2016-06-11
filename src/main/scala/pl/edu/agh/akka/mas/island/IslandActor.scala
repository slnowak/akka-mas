package pl.edu.agh.akka.mas.island

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import org.apache.commons.math3.random.RandomDataGenerator
import pl.edu.agh.akka.mas.UglyStaticGlobalRandomGenerator
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.IslandActor.SpawnNewAgents
import pl.edu.agh.akka.mas.island.MigrationArena.{Agent, KillAgents, PerformMigration}
import pl.edu.agh.akka.mas.island.rastrigin.Worker.{ExchangeResult, RequestMutation}
import pl.edu.agh.akka.mas.island.rastrigin._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by novy on 09.04.16.
  */
class IslandActor(var neighbours: List[ActorSelection], random: RandomDataGenerator, worker: ActorRef) extends Actor with ActorLogging {

  // todo fix it later
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception =>
        log.warning("exception occurred")
        Restart
    }
  val problemSize = 5
  val migrationArena: ActorRef = createMigrationArena()
  val resultExchangeArena: ActorRef = createResultExchangeArena()
  val mutationArena: ActorRef = createMutationArena()

  override def preStart(): Unit = {
    context.system.scheduler.schedule(10 seconds, 10 seconds, self, "share_system_info")
  }

  override def receive(): Receive = handleNeighbourhoodChanges orElse handleWorkersLifecycle orElse handleWorkersRequests

  private def handleNeighbourhoodChanges: Receive = {
    case msg@NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours
      migrationArena forward msg
      resultExchangeArena forward msg
  }

  private def handleWorkersLifecycle: Receive = {
    case SpawnNewAgents(initialSolution) =>
      log.info(s"got request to create new workers from ${sender()}, with data: $initialSolution")
    //      val newWorkers: List[ActorRef] = initialSolution map spawnAgent

    case KillAgents(agentAddresses) =>
      agentAddresses foreach killAgent
  }

  private def killAgent(agent: ActorRef): Unit = agent ! PoisonPill

  private def handleWorkersRequests: Receive = {
    case msg@PerformMigration =>
      migrationArena forward msg

    case msg@ExchangeResult =>
      resultExchangeArena forward msg

    case RequestMutation(feature) =>
    //      mutationArena ! Mutate(Agent(feature, sender()))
  }

  private def createMigrationArena(): ActorRef =
    context.actorOf(MigrationArena.props(neighbours, self), "MigrationArena")

  private def createResultExchangeArena(): ActorRef = context.actorOf(
    ResultExchangeArena.props(neighbours, startingSolution())
  )

  private def startingSolution(): RastriginSolution = RastriginSolution(Double.MaxValue)

  private def createMutationArena(): ActorRef = context.actorOf(
    MutationArena.props()
  )

  //  private def initialWorkers(): Set[ActorRef] = {
  //    (for (i <- 0 to workers) yield spawnAgent()) toSet
  //  }

  private def spawnAgent(): ActorRef = spawnAgent(startingFeature())

  private def spawnAgent(startingFeature: RastriginFeature): ActorRef =
    context.actorOf(Worker.props(new RastriginProblem()))

  private def startingFeature(): RastriginFeature =
    RastriginFeature(
      Array.fill(problemSize) {
        random.nextUniform(-5.12, 5.12)
      }
    )
}

object IslandActor {
  def props(worker: ActorRef,
            neighbours: List[ActorSelection] = List(),
            random: RandomDataGenerator = UglyStaticGlobalRandomGenerator.defaultRandomGenerator()): Props =
    Props(new IslandActor(neighbours, random, worker))

  case class SolutionEvaluated(feature: RastriginFeature, solution: RastriginSolution)

  case class SpawnNewAgents(agents: List[Agent])

}


