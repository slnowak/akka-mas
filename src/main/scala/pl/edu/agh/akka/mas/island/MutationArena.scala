package pl.edu.agh.akka.mas.island

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import pl.edu.agh.akka.mas.island.MigrationArena.Agent
import pl.edu.agh.akka.mas.island.MutationArena.{ApplyNewFeature, Mutate}
import pl.edu.agh.akka.mas.island.ProbabilisticMutationStrategy.MutationParams
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

/**
  * Created by novy on 24.05.16.
  */
class MutationArena(mutationStrategy: MutationStrategy) extends Actor with ActorLogging {


//  override def receive: Receive = {
//    case (Mutate(agentAndHisFeature)) =>
//      askAgentToUpdateHisFeatures(agentAndHisFeature)
//  }

//  private def askAgentToUpdateHisFeatures(agent: Agent): Unit = {
//    agent.agentActor ! ApplyNewFeature(mutationStrategy.mutate(agent.feature))
//  }
//  todo fix
  override def receive: Receive = ???
}

object MutationArena {
  val defaultMutationParams = MutationParams(mutationChance = 0.75, mutationRate = 0.1)

  def props(mutationStrategy: MutationStrategy = ProbabilisticMutationStrategy(defaultMutationParams)): Props =
    Props(new MutationArena(mutationStrategy))

  case class Mutate(agent: Agent)

  case class ApplyNewFeature(feature: RastriginFeature)

}

trait MutationStrategy {
  def mutate(feature: RastriginFeature): RastriginFeature
}
