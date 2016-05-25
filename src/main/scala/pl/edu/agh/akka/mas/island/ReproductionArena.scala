package pl.edu.agh.akka.mas.island

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import pl.edu.agh.akka.mas.island.MigrationArena.{Agent, SpawnNewAgents}
import pl.edu.agh.akka.mas.island.rastrigin.AgentActor.RequestReproduction
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

/**
  * Created by novy on 24.05.16.
  */
class ReproductionArena(thisIsland: ActorRef, reproductionStrategy: ReproductionStrategy)
  extends Actor with ActorLogging {

  var agentsToReproduce: List[Agent] = List.empty

  override def receive: Receive = {
    case RequestReproduction(feature) if agentsToReproduce.size + 1 == 2 =>
      agentsToReproduce = Agent(feature, sender()) :: agentsToReproduce

      val (f1, f2) = agentsToReproduce.map(_.feature) match {
        case List(a, b, _*) => (a, b)
      }
      val (f3, f4): (RastriginFeature, RastriginFeature) = reproductionStrategy.reproduce(f1, f2)
      // TODO: spawn new actors with energy of parents
      thisIsland ! SpawnNewAgents(List(f3, f4))

      agentsToReproduce = List.empty

    case RequestReproduction(feature) =>
      agentsToReproduce = Agent(feature, sender()) :: agentsToReproduce

  }
}

trait ReproductionStrategy {
  def reproduce(feature1: RastriginFeature, feature2: RastriginFeature): (RastriginFeature, RastriginFeature)
}

object ReproductionArena {

  def props(relatedIsland: ActorRef, reproductionStrategy: ReproductionStrategy = ProbabilisticReproductionStrategy()): Props =
    Props(new ReproductionArena(relatedIsland, reproductionStrategy))

  case class SpawnNewAgents(initialFeatures: List[RastriginFeature])

  case class KillAgents(addresses: List[ActorRef])

}
