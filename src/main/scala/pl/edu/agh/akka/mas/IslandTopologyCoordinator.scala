package pl.edu.agh.akka.mas

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

/**
  * Created by novy on 09.04.16.
  */
class IslandTopologyCoordinator extends Actor with ActorLogging {

  val cluster = Cluster(context.system)


  override def preStart(): Unit = subscribeToClusterChanges()

  override def postStop(): Unit = unsubscribeFromCluster()

  override def receive: Receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case ReachableMember(member) =>
      log.info("Member is reachable again {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }

  private def subscribeToClusterChanges(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  private def unsubscribeFromCluster(): Unit = cluster.unsubscribe(self)
}
