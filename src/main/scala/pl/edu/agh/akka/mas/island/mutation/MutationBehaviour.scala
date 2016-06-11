package pl.edu.agh.akka.mas.island.mutation

import pl.edu.agh.akka.mas.island.migration.MigrationArena.Agent
import pl.edu.agh.akka.mas.island.mutation.ProbabilisticMutationStrategy.MutationParams
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

/**
  * Created by novy on 24.05.16.
  */
class MutationBehaviour(mutationStrategy: MutationStrategy) {

  def mutate(agents: List[Agent]): List[Agent] = {
    val features: List[RastriginFeature] = agents.map(_.feature)
    val mutatedFeatures = features.map(mutationStrategy.mutate)
    mutatedFeatures.map(Agent.apply)
  }
}

object MutationBehaviour {
  val defaultMutationParams = MutationParams(mutationChance = 0.75, mutationRate = 0.1)

  def apply(mutationStrategy: MutationStrategy = ProbabilisticMutationStrategy(defaultMutationParams)): MutationBehaviour = {
    new MutationBehaviour(mutationStrategy)
  }
}

trait MutationStrategy {
  def mutate(feature: RastriginFeature): RastriginFeature
}
