package pl.edu.agh.akka.mas

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import pl.edu.agh.akka.mas.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.topology.{CircleTopology, Island, IslandTopology}

/**
  * Created by novy on 09.04.16.
  */
class IslandTopologyCoordinator(islandActor: ActorRef) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val thisIsland = Island(cluster.selfAddress)
  //  todo hardcoded
  var topology: IslandTopology = CircleTopology()

  override def preStart(): Unit = subscribeToClusterChanges()

  override def postStop(): Unit = unsubscribeFromCluster()

  override def receive: Receive = {
    case MemberUp(member) =>
      addIslandToTopology(member.address)
      notifyIslandActorAboutNeighbourhoodChange()

    case ReachableMember(member) =>
      addIslandToTopology(member.address)
      notifyIslandActorAboutNeighbourhoodChange()

    case UnreachableMember(member) =>
      removeIslandFromTopology(member.address)
      notifyIslandActorAboutNeighbourhoodChange()

    case MemberRemoved(member, _) =>
      removeIslandFromTopology(member.address)
      notifyIslandActorAboutNeighbourhoodChange()
  }

  private def addIslandToTopology(islandAddress: Address): Unit = {
    topology = topology.withNew(Island(islandAddress))
  }

  def removeIslandFromTopology(islandAddress: Address): Unit = {
    topology = topology.withoutExisting(Island(islandAddress))
  }

  private def notifyIslandActorAboutNeighbourhoodChange(): Unit = {
    islandActor ! NeighboursChanged(topology.neighboursOf(thisIsland).map(toActorSelection))
  }

  private def toActorSelection(island: Island): ActorSelection = {
    context.actorSelection(s"akka.tcp://${island.islandAddress.hostPort}/user/island")
  }

  private def subscribeToClusterChanges(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  private def unsubscribeFromCluster(): Unit = cluster.unsubscribe(self)
}

object IslandTopologyCoordinator {
  def props(islandActor: ActorRef): Props = Props(new IslandTopologyCoordinator(islandActor))

  case class NeighboursChanged(neighbours: List[ActorSelection])

}

