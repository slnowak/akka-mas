package pl.edu.agh.akka.mas.cluster.management.topology

import akka.actor.Address
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by novy on 06.04.16.
  */
class RingTopologyTest extends FlatSpec with Matchers {

  implicit def strToAddress(str: String): Address = Address("akka", str)

  "A Ring Topology" should "return no neighbours given 1 Island topology" in {
    // given
    val emptyTopology: RingTopology = RingTopology()
    val soleIsland: Island = Island("addr1")
    val withOneIsland: IslandTopology = emptyTopology.withNew(soleIsland)

    // expect
    withOneIsland neighboursOf soleIsland should be(List())
  }

  it should "return an empty neighbours list in case of island outside topology" in {
    // given
    val topology: RingTopology = RingTopology()

    // expect
    topology neighboursOf Island("666") should be(List())
  }

  it should "return only 1 neighbour in case of 2 element topology" in {
    // given
    val firstIsland: Island = Island("addr1")
    val secondIsland: Island = Island("addr2")
    val topology: IslandTopology = RingTopology().withNew(firstIsland).withNew(secondIsland)

    // expect
    topology neighboursOf firstIsland should be(List(secondIsland))
    topology neighboursOf secondIsland should be(List(firstIsland))
  }

  it should "return previous and next island as neighbours for middle-aligned island" in {
    // given
    val topology = RingTopology()
      .withNew(Island("addr1"))
      .withNew(Island("addr2"))
      .withNew(Island("addr3"))
      .withNew(Island("addr4"))

    // expect
    topology neighboursOf Island("addr2") should be(List(Island("addr1"), Island("addr3")))
  }

  it should "return previous and next island with respect to ring ordering for first island" in {
    // given
    val topology = RingTopology()
      .withNew(Island("addr1"))
      .withNew(Island("addr2"))
      .withNew(Island("addr3"))
      .withNew(Island("addr4"))

    // expect
    topology neighboursOf Island("addr1") should be(List(Island("addr4"), Island("addr2")))
  }

  it should "return previous and next island with respect to ring ordering for last island" in {
    // given
    val topology = RingTopology()
      .withNew(Island("addr1"))
      .withNew(Island("addr2"))
      .withNew(Island("addr3"))
      .withNew(Island("addr4"))

    // expect
    topology neighboursOf Island("addr4") should be(List(Island("addr3"), Island("addr1")))
  }

  it should "remove island from topology on request" in {
    // given
    val topology = RingTopology()
      .withNew(Island("addr1"))
      .withNew(Island("addr2"))
      .withNew(Island("addr3"))
      .withNew(Island("addr4"))

    // when
    val withoutOneIsland = topology withoutExisting Island("addr2")

    // then
    withoutOneIsland neighboursOf Island("addr3") should be(List(Island("addr1"), Island("addr4")))
  }
}
