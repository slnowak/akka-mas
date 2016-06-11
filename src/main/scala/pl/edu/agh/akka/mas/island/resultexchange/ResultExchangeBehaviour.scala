package pl.edu.agh.akka.mas.island.resultexchange

import akka.actor.ActorRef
import pl.edu.agh.akka.mas.island.rastrigin.RastriginSolution
import pl.edu.agh.akka.mas.island.resultexchange.ResultExchangeArena.NewLocalSolution

/**
  * Created by novy on 11.06.16.
  */
class ResultExchangeBehaviour(resultExchangeArena: ActorRef) {

  def exchangeSolution(solution: RastriginSolution): Unit = {
    resultExchangeArena ! NewLocalSolution(solution)
  }

}

object ResultExchangeBehaviour {
  def apply(resultExchangeArena: ActorRef): ResultExchangeBehaviour = new ResultExchangeBehaviour(resultExchangeArena)
}