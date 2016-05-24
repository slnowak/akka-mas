package pl.edu.agh.akka.mas.island.rastrigin

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.commons.math3.random.{RandomDataGenerator, RandomGenerator, Well19937c}
import pl.edu.agh.akka.mas.island.rastrigin.AgentActor.{ExchangeResult, RequestMigration}

import scala.concurrent.duration._
import scala.util.Random

/**
  * Created by novy on 10.04.16.
  */
class AgentActor(var feature: RastriginFeature,
                 problem: RastriginProblem,
                 island: ActorRef)
  extends Actor with ActorLogging {

  import context.dispatcher

  def random = RandomComponent.randomData

  val mutationChance = 0.75
  val mutationRate = 0.1

  override def preStart(): Unit = {
    context.system.scheduler.schedule(10 seconds, 10 seconds, self, "migrate")
    context.system.scheduler.schedule(10 seconds, 20 second, self, "exchange result")
    context.system.scheduler.scheduleOnce(10 seconds, self, "transform")
  }

  override def receive: Receive = {
    case "migrate" =>
      island ! RequestMigration(feature)

    case "exchange result" =>
      val solution: RastriginSolution = problem.evaluate(feature)
      log.info(s"Exchanging result ${solution}")
      island ! ExchangeResult(solution)

    case "transform" =>
      feature = transform(feature)
  }

  def transform(feature: RastriginFeature): RastriginFeature = mutateFeature(feature)

  def mutateFeature(feature: RastriginFeature): RastriginFeature =
    if (Random.nextDouble() < mutationChance)
      RastriginFeature(feature.coordinates.map(f => if (Random.nextDouble() < mutationRate) mutateElement(f) else f))
    else
      feature

  def mutateElement(f: Double): Double = {
    val range = Random.nextDouble() match {
      case x if x < 0.2 => 5.0
      case x if x < 0.4 => 0.2
      case _ => 1.0
    }

    f + range * random.nextCauchy(0.0, 1.0)
  }

}

object AgentActor {
  def props(feature: RastriginFeature, problem: RastriginProblem, island: ActorRef): Props =
    Props(new AgentActor(feature, problem, island))

  case class RequestMigration(feature: RastriginFeature)

  case class ExchangeResult(solution: RastriginSolution)

}

object RandomComponent {
  def randomGeneratorFactory(seed: Long): RandomGenerator = new Well19937c(seed)

  lazy val randomData = new RandomDataGenerator(randomGeneratorFactory(System.currentTimeMillis()))
}
