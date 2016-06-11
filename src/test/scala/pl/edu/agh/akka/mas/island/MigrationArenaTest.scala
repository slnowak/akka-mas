package pl.edu.agh.akka.mas.island

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.testkit._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, WordSpecLike}
import pl.edu.agh.akka.mas.island.MigrationArena.{KillAgents, SpawnNewAgents}
import pl.edu.agh.akka.mas.island.rastrigin.Worker.RequestMigration
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature


/**
  * Created by novy on 10.04.16.
  */
class MigrationArenaTest extends TestKit(ActorSystem()) with WordSpecLike with BeforeAndAfterEach with BeforeAndAfterAll with ImplicitSender {

  var objectUnderTest: ActorRef = _
  var testProbe: TestProbe = _
  var relatedIsland: TestProbe = _
  var soleNeighbour: TestProbe = _

  override protected def beforeEach(): Unit = {
    testProbe = TestProbe()
    relatedIsland = TestProbe()
    soleNeighbour = TestProbe()

    val containsOnlyOneNeighbour: List[ActorSelection] = List(system.actorSelection(soleNeighbour.ref.path))
    objectUnderTest = TestActorRef(MigrationArena.props(
      neighbours = containsOnlyOneNeighbour,
      relatedIsland = relatedIsland.ref,
      requiredAgentsToMigrate = 2
    ))
  }

  override protected def afterAll(): Unit = system.terminate()

  "Migration arena " must {

    "not send any message if migration threshold not exceeded" in {
      // when
      testProbe.send(objectUnderTest, RequestMigration(exampleFeature()))

      // then
      relatedIsland expectNoMsg()
      soleNeighbour expectNoMsg()
    }

    "ask random neighbour to create new agents if migration started" in {
      // given
      val firstAgentFeature: RastriginFeature = exampleFeature()
      objectUnderTest ! RequestMigration(firstAgentFeature)

      // when
      val secondAgentFeature: RastriginFeature = exampleFeature()
      objectUnderTest ! RequestMigration(secondAgentFeature)

      // then
      soleNeighbour expectMsg SpawnNewAgents(List(firstAgentFeature, secondAgentFeature))
    }

    "request killing agents on related island if migration started" in {
      // given
      val firstAgent = TestProbe()
      val secondAgent = TestProbe()

      val firstAgentFeature: RastriginFeature = exampleFeature()
      firstAgent.send(objectUnderTest, RequestMigration(firstAgentFeature))

      // when
      val secondAgentFeature: RastriginFeature = exampleFeature()
      secondAgent.send(objectUnderTest, RequestMigration(secondAgentFeature))

      // then
      relatedIsland expectMsg KillAgents(List(firstAgent.ref, secondAgent.ref))
    }
  }

  private def exampleFeature(): RastriginFeature = RastriginFeature(Array(66, 11, 22))
}
