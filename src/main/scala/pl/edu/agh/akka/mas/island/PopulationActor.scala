package pl.edu.agh.akka.mas.island

import akka.actor._
import org.apache.commons.math3.random.RandomDataGenerator
import pl.edu.agh.akka.mas.UglyStaticGlobalRandomGenerator
import pl.edu.agh.akka.mas.island.PopulationActor.IslandBehaviours
import pl.edu.agh.akka.mas.island.migration.MigrationArena.Agent
import pl.edu.agh.akka.mas.island.migration.MigrationBehaviour
import pl.edu.agh.akka.mas.island.mutation.MutationBehaviour
import pl.edu.agh.akka.mas.island.rastrigin._
import pl.edu.agh.akka.mas.island.resultexchange.ResultExchangeBehaviour

/**
  * Created by novy on 09.04.16.
  */
class PopulationActor(random: RandomDataGenerator, worker: ActorRef, behaviours: IslandBehaviours) extends Actor with ActorLogging {

  val problemSize = 5
  //  val mutationArena: ActorRef = createMutationArena()


  override def receive(): Receive = ???

  //  private def handleNeighbourhoodChanges: Receive = {
  //    case msg@NeighboursChanged(newNeighbours) =>
  //      this.neighbours = newNeighbours
  //      migrationArena forward msg
  //      resultExchangeArena forward msg
  //  }
  //
  //  private def handleWorkersLifecycle: Receive = {
  //    case SpawnNewAgents(initialSolution) =>
  //      log.info(s"got request to create new workers from ${sender()}, with data: $initialSolution")
  //      val newWorkers: List[ActorRef] = initialSolution map spawnAgent

  //    case KillAgents(agentAddresses) =>
  //      agentAddresses foreach killAgent
  //  }

  //  private def killAgent(agent: ActorRef): Unit = agent ! PoisonPill
  //
  //  private def handleWorkersRequests: Receive = {
  //    case msg@PerformMigration =>
  //      migrationArena forward msg
  //
  //    case msg@ExchangeResult =>
  //      resultExchangeArena forward msg
  //
  //    case RequestMutation(feature) =>
  //    //      mutationArena ! Mutate(Agent(feature, sender()))
  //  }


  private def startingSolution(): RastriginSolution = RastriginSolution(Double.MaxValue)

  //  private def createMutationArena(): ActorRef = context.actorOf(
  //    MutationArena.props()
  //  )

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

object PopulationActor {
  def props(worker: ActorRef,
            behaviours: IslandBehaviours,
            random: RandomDataGenerator = UglyStaticGlobalRandomGenerator.defaultRandomGenerator()): Props =
    Props(new PopulationActor(random, worker, behaviours))

  case class SolutionEvaluated(feature: RastriginFeature, solution: RastriginSolution)

  case class SpawnNewAgents(agents: List[Agent])

  case class IslandBehaviours(mutation: MutationBehaviour,
                              migration: MigrationBehaviour,
                              resultExchange: ResultExchangeBehaviour)

}


