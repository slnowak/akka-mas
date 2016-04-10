package pl.edu.agh.akka.mas.island

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.IslandActor.HelloFromTheOtherSiiiiiiiiiiiideeeee

/**
  * Created by novy on 09.04.16.
  */
class IslandActor(var neighbours: List[ActorSelection], workers: Int) extends Actor with ActorLogging {

  override def receive: Receive = {
    case NeighboursChanged(newNeighbours) =>
      this.neighbours = newNeighbours
      sayHelloTo(newNeighbours)
      log.info(s"new neighbours: $neighbours")

    case hello@HelloFromTheOtherSiiiiiiiiiiiideeeee(_) =>
      log.info(s"message from different node: $hello")
  }

  def sayHelloTo(newNeighbours: List[ActorSelection]) = {
    //    todo just for testing purposes
    neighbours foreach (_ ! HelloFromTheOtherSiiiiiiiiiiiideeeee(self.path.address.hostPort))
  }
}

object IslandActor {
  def props(neighbours: List[ActorSelection] = List(), workers: Int = 10): Props =
    Props(new IslandActor(neighbours, workers))

  case class HelloFromTheOtherSiiiiiiiiiiiideeeee(sender: String)

}

