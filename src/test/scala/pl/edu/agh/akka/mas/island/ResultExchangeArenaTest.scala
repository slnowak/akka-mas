package pl.edu.agh.akka.mas.island

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, WordSpecLike}
import pl.edu.agh.akka.mas.island.AgentActor.{ExchangeResult, JoinArena}
import pl.edu.agh.akka.mas.island.MigrationArena.AgentState
import pl.edu.agh.akka.mas.island.ResultExchangeArena.NewResultArrived

/**
  * Created by novy on 13.04.16.
  */
class ResultExchangeArenaTest extends TestKit(ActorSystem()) with WordSpecLike with BeforeAndAfterEach with BeforeAndAfterAll with ImplicitSender {

  var objectUnderTest: ActorRef = _
  var soleNeighbour: TestProbe = _

  override protected def beforeEach(): Unit = {
    val containsOnlyOneNeighbour: List[ActorSelection] = List(system.actorSelection(soleNeighbour.ref.path))
    objectUnderTest = TestActorRef(ResultExchangeArena.props(neighbours = containsOnlyOneNeighbour))
  }

  "Result exchange arena " must {
    "forward information message to all actors on the island" in {
      // given
      val firstWorker: TestProbe = TestProbe()

      // when
      val resultToSend: AgentState = randomAgentState()
      firstWorker.send(objectUnderTest, ExchangeResult(resultToSend))

      // then
      soleNeighbour expectMsg NewResultArrived(firstWorker.ref, resultToSend)
    }
  }

  override protected def afterAll(): Unit = system.terminate()

  private def randomAgentState(): AgentState = {
    new AgentState {}
  }
}
