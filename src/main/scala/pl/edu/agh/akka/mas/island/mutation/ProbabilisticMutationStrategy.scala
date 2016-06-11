package pl.edu.agh.akka.mas.island.mutation

import org.apache.commons.math3.random.RandomDataGenerator
import pl.edu.agh.akka.mas.UglyStaticGlobalRandomGenerator
import pl.edu.agh.akka.mas.island.mutation.ProbabilisticMutationStrategy.MutationParams
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

import scala.util.Random

/**
  * Created by novy on 24.05.16.
  */
class ProbabilisticMutationStrategy(random: RandomDataGenerator, params: MutationParams) extends MutationStrategy {

  override def mutate(feature: RastriginFeature): RastriginFeature = {
    if (probability() < params.mutationChance) mutated(feature) else feature
  }

  private def mutated(feature: RastriginFeature): RastriginFeature = {
    RastriginFeature(
      feature.coordinates.map(f => if (probability() < params.mutationRate) mutateElement(f) else f)
    )
  }

  private def mutateElement(f: Double): Double = {
    val range = Random.nextDouble() match {
      case x if x < 0.2 => 5.0
      case x if x < 0.4 => 0.2
      case _ => 1.0
    }

    f + range * probability()
  }

  private def probability(): Double = {
    random.nextUniform(0.0, 1.0)
  }
}

object ProbabilisticMutationStrategy {

  def apply(mutationParams: MutationParams): ProbabilisticMutationStrategy = new ProbabilisticMutationStrategy(
    UglyStaticGlobalRandomGenerator.defaultRandomGenerator(), mutationParams
  )

  case class MutationParams(mutationChance: Double, mutationRate: Double)

}

