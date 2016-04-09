package pl.edu.agh.akka.mas.topology

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by novy on 06.04.16.
  */
class CircleTopologyTest extends FlatSpec with Matchers {

  "A Circle Topology" should "return no neighbours given 1 Island topology" in {
    // given
    val emptyTopology: CircleTopology = CircleTopology()
    val soleIsland: Island = Island(1)
    val withOneIsland: IslandTopology = emptyTopology.withNew(soleIsland)

    // expect
    withOneIsland neighboursOf soleIsland should be(List())
  }

  it should "return an empty neighbours list in case of island outside topology" in {
    // given
    val topology: CircleTopology = CircleTopology()

    // expect
    topology neighboursOf Island(666) should be(List())
  }

  it should "return only 1 neighbour in case of 2 element topology" in {
    // given
    val firstIsland: Island = Island(1)
    val secondIsland: Island = Island(2)
    val topology: IslandTopology = CircleTopology().withNew(firstIsland).withNew(secondIsland)

    // expect
    topology neighboursOf firstIsland should be(List(secondIsland))
    topology neighboursOf secondIsland should be(List(firstIsland))
  }

  it should "return previous and next island as neighbours for middle-aligned island" in {
    // given
    val topology = CircleTopology()
      .withNew(Island(1))
      .withNew(Island(2))
      .withNew(Island(3))
      .withNew(Island(4))

    // expect
    topology neighboursOf Island(2) should be(List(Island(1), Island(3)))
  }

  it should "return previous and next island with respect to ring ordering for first island" in {
    // given
    val topology = CircleTopology()
      .withNew(Island(1))
      .withNew(Island(2))
      .withNew(Island(3))
      .withNew(Island(4))

    // expect
    topology neighboursOf Island(1) should be(List(Island(4), Island(2)))
  }

  it should "return previous and next island with respect to ring ordering for last island" in {
    // given
    val topology = CircleTopology()
      .withNew(Island(1))
      .withNew(Island(2))
      .withNew(Island(3))
      .withNew(Island(4))

    // expect
    topology neighboursOf Island(4) should be(List(Island(3), Island(1)))
  }

  it should "remove island from topology on request" in {
    // given
    val topology = CircleTopology()
      .withNew(Island(1))
      .withNew(Island(2))
      .withNew(Island(3))
      .withNew(Island(4))

    // when
    val withoutOneIsland = topology withoutExisting Island(2)

    // then
    withoutOneIsland neighboursOf Island(3) should be(List(Island(1), Island(4)))
  }

}
