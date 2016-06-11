package pl.edu.agh.akka.mas.island.rastrigin

import akka.actor.{Actor, ActorLogging, Props}
import pl.edu.agh.akka.mas.island.IslandActor.SolutionEvaluated
import pl.edu.agh.akka.mas.island.rastrigin.Worker.PerformWork

/**
  * Created by novy on 10.04.16.
  */
class Worker(problem: RastriginProblem) extends Actor with ActorLogging {

  override def receive: Receive = performWork

  private def performWork: Receive = {
    case PerformWork(feature) =>
      sender() ! SolutionEvaluated(feature, evaluateFeature(feature))
  }

  private def evaluateFeature(feature: RastriginFeature): RastriginSolution = problem.evaluate(feature)
}

object Worker {
  def props(problem: RastriginProblem): Props = Props(new Worker(problem))

  case class RequestMutation(feature: RastriginFeature)

  case class ExchangeResult(solution: RastriginSolution)

  case class PerformWork(features: RastriginFeature)

}
