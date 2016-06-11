package pl.edu.agh.akka.mas.island.rastrigin

import akka.actor.{Actor, ActorLogging, Props}
import pl.edu.agh.akka.mas.island.PopulationActor.SolutionEvaluated
import pl.edu.agh.akka.mas.island.rastrigin.Worker.EvaluateFeature

/**
  * Created by novy on 10.04.16.
  */
class Worker(problem: RastriginProblem) extends Actor with ActorLogging {

  override def receive: Receive = performComputation

  private def performComputation: Receive = {
    case EvaluateFeature(feature) =>
      log.info(s"got feature $feature to evaluate")
      sender() ! SolutionEvaluated(feature, evaluateFeature(feature))
  }

  private def evaluateFeature(feature: RastriginFeature): RastriginSolution = problem.evaluate(feature)
}

object Worker {
  def props(problem: RastriginProblem): Props = Props(new Worker(problem))

  case class RequestMutation(feature: RastriginFeature)

  case class ExchangeResult(solution: RastriginSolution)

  case class EvaluateFeature(feature: RastriginFeature)

}
