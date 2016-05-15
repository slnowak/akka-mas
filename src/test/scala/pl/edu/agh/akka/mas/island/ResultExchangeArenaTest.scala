package pl.edu.agh.akka.mas.island

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, WordSpecLike}
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.ResultExchangeArena.{BestSolutionQuery, GlobalSolution, NewGlobalSolution, NewLocalSolution}
import pl.edu.agh.akka.mas.problems.RastriginAgent.RastriginSolution

/**
  * Created by novy on 13.04.16.
  */
class ResultExchangeArenaTest extends TestKit(ActorSystem()) with WordSpecLike with BeforeAndAfterEach with BeforeAndAfterAll with ImplicitSender {
  val STARTING_SOLUTION = RastriginSolution(0)

  var objectUnderTest: ActorRef = _
  var soleNeighbour: TestProbe = _
  var testProbe: TestProbe = _

  override protected def beforeEach(): Unit = {
    testProbe = TestProbe()
    soleNeighbour = TestProbe()
    val containsOnlyOneNeighbour: List[ActorSelection] = List(system.actorSelection(soleNeighbour.ref.path))

    objectUnderTest = TestActorRef(ResultExchangeArena.props(
      neighbours = containsOnlyOneNeighbour,
      startingSolution = STARTING_SOLUTION)
    )
  }

  "Result exchange arena " must {
    "respond with best solution when asked" in {
      // when
      testProbe.send(objectUnderTest, BestSolutionQuery)

      // then
      testProbe expectMsg GlobalSolution(objectUnderTest, STARTING_SOLUTION)
    }

    "forward best solution to all neighbours given better local solution" in {
      // given
      val firstWorker: TestProbe = TestProbe()

      // when
      val betterLocalSolution: NewLocalSolution = localSolutionWith(666)
      firstWorker.send(objectUnderTest, betterLocalSolution)

      // then
      soleNeighbour expectMsg NewGlobalSolution(GlobalSolution(objectUnderTest, betterLocalSolution.solution))
    }

    "properly update neighbours if needed" in {
      // given
      val newNeighbour1 = TestProbe()
      val newNeighbour2 = TestProbe()
      testProbe.send(
        objectUnderTest,
        NeighboursChanged(
          List(system.actorSelection(newNeighbour1.ref.path), system.actorSelection(newNeighbour2.ref.path))
        )
      )

      // when
      testProbe.send(objectUnderTest, localSolutionWith(STARTING_SOLUTION.result + 1))

      // then
      val expectedGlobalSolution = globalSolutionWith(objectUnderTest, STARTING_SOLUTION.result + 1)
      newNeighbour1 expectMsg expectedGlobalSolution
      newNeighbour2 expectMsg expectedGlobalSolution
    }

    "don't forward anything to neighbours if solution is worse" in {
      // given
      val worseSolution: NewLocalSolution = localSolutionWith(STARTING_SOLUTION.result - 666)
      testProbe.send(objectUnderTest, worseSolution)

      // when
      testProbe.send(objectUnderTest, BestSolutionQuery)

      // then
      testProbe expectMsg GlobalSolution(objectUnderTest, STARTING_SOLUTION)
    }

    "update it's internal solution when new global arrives" in {
      // given
      val newGlobalSolution: NewGlobalSolution = globalSolutionWith(soleNeighbour.ref, STARTING_SOLUTION.result + 1)
      testProbe.send(objectUnderTest, newGlobalSolution)

      // when
      testProbe.send(objectUnderTest, BestSolutionQuery)

      // then
      testProbe expectMsg newGlobalSolution.solution
    }

    "ignore global solution if it's worse" in {
      // given
      val newGlobalSolution: NewGlobalSolution = globalSolutionWith(soleNeighbour.ref, STARTING_SOLUTION.result - 1)
      objectUnderTest ! newGlobalSolution

      // when
      testProbe.send(objectUnderTest, BestSolutionQuery)

      // then
      testProbe expectMsg GlobalSolution(objectUnderTest, STARTING_SOLUTION)
    }
  }

  def localSolutionWith(value: Float): NewLocalSolution = NewLocalSolution(RastriginSolution(value))

  def globalSolutionWith(senderIsland: ActorRef, value: Float): NewGlobalSolution = NewGlobalSolution(GlobalSolution(senderIsland, RastriginSolution(value)))

  override protected def afterAll(): Unit = system.terminate()
}

