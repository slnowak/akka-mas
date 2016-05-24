package pl.edu.agh.akka.mas.island.rastrigin

/**
  * Created by novy on 24.05.16.
  */
case class RastriginFeature(coordinates: Seq[Double])

case class RastriginSolution(value: Double) {
  def betterThan(another: RastriginSolution): Boolean = this.value < another.value
}

class RastriginProblem() {
  def evaluate(feature: RastriginFeature): RastriginSolution =
    RastriginSolution(
      feature.coordinates.foldLeft(0.0)((sum, x) => sum + valueForSingleFeature(x))
    )

  private def valueForSingleFeature(x: Double): Double =
    10 + x * x - 10 * math.cos(2 * x * math.Pi)
}
