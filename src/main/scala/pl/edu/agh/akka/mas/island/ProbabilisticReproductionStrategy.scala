package pl.edu.agh.akka.mas.island

import org.apache.commons.math3.random.RandomDataGenerator
import pl.edu.agh.akka.mas.UglyStaticGlobalRandomGenerator
import pl.edu.agh.akka.mas.island.ProbabilisticMutationStrategy.MutationParams
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

/**
  * Created by ania on 5/25/16.
  */
class ProbabilisticReproductionStrategy(random: RandomDataGenerator, mutationStrategy: MutationStrategy)
  extends ReproductionStrategy {

  override def reproduce(feature1: RastriginFeature, feature2: RastriginFeature): (RastriginFeature, RastriginFeature) =
    mutateFatures(recombineFeatures(feature1, feature2))

  def mutateFatures(f: (RastriginFeature, RastriginFeature)) = f match {
    case (feature1, feature2) =>
      (mutationStrategy.mutate(feature1), mutationStrategy.mutate(feature2))
  }

  def recombineFeatures(f1: RastriginFeature, f2: RastriginFeature): (RastriginFeature, RastriginFeature) = {
    val (f3, f4) = f1.coordinates.zip(f2.coordinates).map(recombineElements).unzip
    (RastriginFeature(f3.toArray), RastriginFeature(f4.toArray))
  }

  def recombineElements(elements: (Double, Double)): (Double, Double) = elements match {
    case e @ (element1, element2) if element1 == element2 => e
    case (element1, element2) =>
      val a = math.min(element1, element2)
      val b = math.max(element1, element2)
      (random.nextUniform(a, b), random.nextUniform(a, b))
  }
}

object ProbabilisticReproductionStrategy {
  val defaultMutationParams = MutationParams(mutationChance = 0.75, mutationRate = 0.1)

  def apply(mutationStrategy: MutationStrategy = ProbabilisticMutationStrategy(defaultMutationParams)): ProbabilisticReproductionStrategy = new ProbabilisticReproductionStrategy(
    UglyStaticGlobalRandomGenerator.defaultRandomGenerator(), mutationStrategy
  )

  case class ReproductionParams(mutationChance: Double, mutationRate: Double)

}


