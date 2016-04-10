package pl.edu.agh.akka.mas.cluster.management.topology

import akka.actor.Address

/**
  * Created by novy on 06.04.16.
  */
trait IslandTopology {

  def neighboursOf(island: Island): List[Island]

  def withNew(island: Island): IslandTopology

  def withoutExisting(island: Island): IslandTopology

}

case class Island(islandAddress: Address)



