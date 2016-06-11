package pl.edu.agh.akka.mas.island

import org.scalatest._
import pl.edu.agh.akka.mas.island.migration.MigrationArena.Agent
import pl.edu.agh.akka.mas.island.mutation.{MutationBehaviour, MutationStrategy}
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

/**
  * Created by novy on 24.05.16.
  */
class MutationBehaviourTest extends FunSpec with BeforeAndAfterEach with Matchers {

  var objectUnderTest: MutationBehaviour = _
  var multiplyEachFeatureBy2: MutationStrategy = _


  override protected def beforeEach(): Unit = {
    multiplyEachFeatureBy2 = new MutationStrategy {
      override def mutate(feature: RastriginFeature): RastriginFeature =
        feature.copy(feature.coordinates map {
          _ * 2
        })
    }
    objectUnderTest = MutationBehaviour(multiplyEachFeatureBy2)
  }

  describe("Mutation arena") {
    it("mutates each agent") {
      // given
      val agentsToMutate: List[Agent] = List(agentWithFeatures(Array(111, 222)), agentWithFeatures(Array(333, 666)))

      // when
      val mutatedAgents = objectUnderTest.mutate(agentsToMutate)

      // then
      mutatedAgents shouldEqual List(agentWithFeatures(Array(222, 444)), agentWithFeatures(Array(666, 1332)))
    }
  }

  def agentWithFeatures(rawValues: Array[Double]): Agent = Agent(RastriginFeature(rawValues))
}
