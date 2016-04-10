package pl.edu.agh.akka.mas.island

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.testkit._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, WordSpecLike}
import pl.edu.agh.akka.mas.island.MigrationArena.{AgentState, CreateNewAgents, JoinArena}

/**
  * Created by novy on 10.04.16.
  */
class MigrationArenaTest extends TestKit(ActorSystem()) with WordSpecLike with BeforeAndAfterEach with BeforeAndAfterAll with ImplicitSender {

  var objectUnderTest: ActorRef = _
  var soleNeighbour: TestProbe = _

  override protected def beforeEach(): Unit = {
    soleNeighbour = TestProbe()
    val containsOnlyOneNeighbour: List[ActorSelection] = List(system.actorSelection(soleNeighbour.ref.path))
    objectUnderTest = TestActorRef(MigrationArena.props(
      neighbours = containsOnlyOneNeighbour,
      requiredAgentsToMigrate = 2
    ))
  }

  override protected def afterAll(): Unit = system.terminate()

  "Migration arena " must {

    "not send any message if migration threshold not exceeded" in {
      // when
      objectUnderTest ! JoinArena(randomAgentState())

      // then
      expectNoMsg()
      soleNeighbour expectNoMsg()
    }

    "ask random neighbour to create new agents if migration started" in {
      // given
      val firstAgentState: AgentState = randomAgentState()
      objectUnderTest ! JoinArena(firstAgentState)

      // when
      val secondAgentState: AgentState = randomAgentState()
      objectUnderTest ! JoinArena(secondAgentState)

      // then
      soleNeighbour expectMsg CreateNewAgents(List(firstAgentState, secondAgentState))
    }

    "kill all agents to migrate if migration started" in {
      // given
      val firstAgent = TestProbe()
      val firstAgentWatcher = TestProbe()
      firstAgentWatcher watch firstAgent.ref

      val secondAgent = TestProbe()
      val secondAgentWatcher = TestProbe()
      secondAgentWatcher watch secondAgent.ref


      val firstAgentState: AgentState = randomAgentState()
      firstAgent.send(objectUnderTest, JoinArena(firstAgentState))

      // when

      val secondAgentState: AgentState = randomAgentState()
      secondAgent.send(objectUnderTest, JoinArena(secondAgentState))

      // then
      firstAgentWatcher expectTerminated firstAgent.ref
      secondAgentWatcher expectTerminated secondAgent.ref
    }
  }

  private def randomAgentState(): AgentState = {
    new AgentState {}
  }

}
