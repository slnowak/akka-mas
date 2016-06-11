package pl.edu.agh.akka.mas.island

import akka.actor._
import org.apache.commons.math3.random.RandomDataGenerator
import pl.edu.agh.akka.mas.UglyStaticGlobalRandomGenerator
import pl.edu.agh.akka.mas.island.PopulationActor.{IslandBehaviours, PerformComputation, SolutionEvaluated, SpawnNewAgents}
import pl.edu.agh.akka.mas.island.migration.MigrationArena.Agent
import pl.edu.agh.akka.mas.island.migration.MigrationBehaviour
import pl.edu.agh.akka.mas.island.mutation.MutationBehaviour
import pl.edu.agh.akka.mas.island.rastrigin.Worker.EvaluateFeature
import pl.edu.agh.akka.mas.island.rastrigin._
import pl.edu.agh.akka.mas.island.resultexchange.ResultExchangeBehaviour

/**
  * Created by novy on 09.04.16.
  */
class PopulationActor(random: RandomDataGenerator, workers: ActorRef, behaviours: IslandBehaviours) extends Actor with ActorLogging {
  val problemSize = 2
  val populationSize = 10

  override def receive: Receive = readyForComputation(randomPopulationOfSize(populationSize))

  private def readyForComputation(population: List[Agent]): Receive = {
    case PerformComputation =>
      context become waitingForComputationResults(population, List())
      requestComputationFor(population)

    case SpawnNewAgents(agents) =>
      log.info(s"accepting migration while waiting for computation: $agents")
      context become readyForComputation(population ++ agents)
  }

  private def waitingForComputationResults(population: List[Agent], solutions: List[RastriginSolution]): Receive = {
    def notAllSolutionsGathered(): Boolean = solutions.size < population.size - 1

    {
      case SpawnNewAgents(agentsToMigrate) =>
        log.info(s"accepting migration while waiting for results: $agentsToMigrate")
        requestComputationFor(agentsToMigrate)
        context become waitingForComputationResults(population ++ agentsToMigrate, solutions)

      case SolutionEvaluated(feature, solution) if notAllSolutionsGathered() =>
        behaviours.solutionExchange.exchangeSolution(solution)
        context become waitingForComputationResults(population, solutions ++ List(solution))

      case SolutionEvaluated(feature, solution) =>
        behaviours.solutionExchange.exchangeSolution(solution)
        val withoutEmigrants = populationWithSomeAgentsMigrated(population)
        context become readyForComputation(behaviours.mutation.mutate(withoutEmigrants))
        self ! PerformComputation
    }
  }

  private def populationWithSomeAgentsMigrated(population: List[Agent]): List[Agent] = {
    val toMigrate: List[Agent] = behaviours.migration.chooseAgentsToMigrate(population)
    behaviours.migration.migrateAgents(toMigrate)
    population diff toMigrate
  }

  private implicit def agentsToFeatures(agents: List[Agent]): List[RastriginFeature] = agents.map(_.feature)

  private def requestComputationFor(features: List[RastriginFeature]): Unit = {
    features foreach (feature => workers ! EvaluateFeature(feature))
  }

  private def randomPopulationOfSize(n: Int): List[Agent] = {
    Stream.continually(Agent(randomFeature())).take(n).toList
  }

  private def randomFeature(): RastriginFeature =
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

  case object PerformComputation

  case class IslandBehaviours(mutation: MutationBehaviour,
                              migration: MigrationBehaviour,
                              solutionExchange: ResultExchangeBehaviour)

}


