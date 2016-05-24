package pl.edu.agh.akka.mas.island

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, WordSpecLike}
import pl.edu.agh.akka.mas.island.MigrationArena.Agent
import pl.edu.agh.akka.mas.island.MutationArena.{ApplyNewFeature, Mutate}
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

/**
  * Created by novy on 24.05.16.
  */
class MutationArenaTest extends TestKit(ActorSystem()) with WordSpecLike with BeforeAndAfterEach with BeforeAndAfterAll with ImplicitSender {

  var objectUnderTest: ActorRef = _
  var testProbe: TestProbe = _
  var multiplyEachFeatureBy2: MutationStrategy = _


  override protected def beforeEach(): Unit = {
    testProbe = TestProbe()

    multiplyEachFeatureBy2 = new MutationStrategy {
      override def mutate(feature: RastriginFeature): RastriginFeature =
        feature.copy(feature.coordinates map {
          _ * 2
        })
    }
    objectUnderTest = TestActorRef(MutationArena.props(multiplyEachFeatureBy2))
  }

  override protected def afterAll(): Unit = system.terminate()

  "Mutation arena" must {

    "send a message with new features to chosen agent when requested" in {
      // given
      val initialFeature: RastriginFeature = featureWith(Array(333, 666))

      // when
      objectUnderTest ! Mutate(Agent(initialFeature, testProbe.ref))

      // then
      testProbe expectMsg ApplyNewFeature(featureWith(Array(666, 1332)))
    }
  }

  def featureWith(rawValues: Array[Double]): RastriginFeature = RastriginFeature(rawValues)
}
