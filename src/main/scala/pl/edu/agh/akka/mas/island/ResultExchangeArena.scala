package pl.edu.agh.akka.mas.island

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.ResultExchangeArena.{BestSolutionQuery, GlobalSolution, NewGlobalSolution, NewLocalSolution}
import pl.edu.agh.akka.mas.problems.RastriginAgent.RastriginSolution

/**
  * Created by novy on 13.04.16.
  */
class ResultExchangeArena(var neighbours: List[ActorSelection], val startingSolution: RastriginSolution) extends Actor with ActorLogging {
  var bestSolution = GlobalSolution(self, startingSolution)

  override def receive: Receive = {
    case NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours

    case BestSolutionQuery =>
      sender() ! bestSolution

    case NewLocalSolution(newSolution) if newSolution betterThan bestSolution.solution =>
      this.bestSolution = GlobalSolution(self, newSolution)
      broadcastSolutionToNeighbours(this.bestSolution)

    case NewGlobalSolution(solutionAndIsland) if solutionAndIsland.solution betterThan bestSolution.solution =>
      this.bestSolution = solutionAndIsland
  }

  def broadcastSolutionToNeighbours(solution: GlobalSolution): Unit = {
    neighbours foreach {
      _ ! NewGlobalSolution(solution)
    }
  }
}

object ResultExchangeArena {
  def props(neighbours: List[ActorSelection], startingSolution: RastriginSolution): Props =
    Props(new ResultExchangeArena(neighbours, startingSolution))

  case class NewGlobalSolution(solution: GlobalSolution)

  case class GlobalSolution(originatingIsland: ActorRef, solution: RastriginSolution)

  case class NewLocalSolution(solution: RastriginSolution)

  object BestSolutionQuery

}
