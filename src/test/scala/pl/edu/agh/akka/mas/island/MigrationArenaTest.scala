package pl.edu.agh.akka.mas.island

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.testkit._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, WordSpecLike}
import pl.edu.agh.akka.mas.island.IslandActor.SpawnNewAgents
import pl.edu.agh.akka.mas.island.MigrationArena.{Agent, PerformMigration}
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
      relatedIsland = relatedIsland.ref
    ))
  }

  override protected def afterAll(): Unit = system.terminate()

  "Migration arena " must {

    "migrate requested agents to random neighbour" in {
      // given
      val firstAgent = Agent(exampleFeature())
      val secondAgentFeature = Agent(exampleFeature())

      // when
      objectUnderTest ! PerformMigration(List(firstAgent, secondAgentFeature))

      // then
      soleNeighbour expectMsg SpawnNewAgents(List(firstAgent, secondAgentFeature))
    }
  }

  private def exampleFeature(): RastriginFeature = RastriginFeature(Array(66, 11, 22))
}
