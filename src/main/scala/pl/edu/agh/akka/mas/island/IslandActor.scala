package pl.edu.agh.akka.mas.island

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import pl.edu.agh.akka.mas.island.IslandActor.InitPopulation
import pl.edu.agh.akka.mas.island.PopulationActor.{IslandBehaviours, PerformComputation}
import pl.edu.agh.akka.mas.island.migration.{AkkaClusterMigrationBehaviour, MigrationArena}
import pl.edu.agh.akka.mas.island.mutation.MutationBehaviour
import pl.edu.agh.akka.mas.island.rastrigin.{RastriginProblem, Worker}
import pl.edu.agh.akka.mas.island.resultexchange.{ResultExchangeArena, ResultExchangeBehaviour}

/**
  * Created by novy on 11.06.16.
  */
class IslandActor() extends Actor with ActorLogging {
  val population: ActorRef = createInitialPopulation()

  override def receive: Receive = {
    case InitPopulation =>
      population ! PerformComputation
  }

  def createInitialPopulation(): ActorRef = {
    context.actorOf(PopulationActor.props(
      workers(),
      IslandBehaviours(mutationBehaviour(), migrationBehaviour(), resultExchangeBehaviour())
    ))
  }

  def workers(): ActorRef = context.actorOf(
    RoundRobinPool(10).props(Worker.props(new RastriginProblem())),
    "workersRouter"
  )

  def resultExchangeBehaviour() = ResultExchangeBehaviour(
    context.actorOf(ResultExchangeArena.props(), "resultExchange")
  )

  def mutationBehaviour(): MutationBehaviour = MutationBehaviour()

  def migrationBehaviour() = AkkaClusterMigrationBehaviour(
    context.actorOf(MigrationArena.props(), "migration")
  )
}

object IslandActor {

  def props(): Props = Props(new IslandActor())

  case object InitPopulation

}
