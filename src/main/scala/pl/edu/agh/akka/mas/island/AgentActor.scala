package pl.edu.agh.akka.mas.island

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pl.edu.agh.akka.mas.island.AgentActor.{ExchangeResult, RastriginSolution, RequestMigration}

import scala.concurrent.duration._
import scala.util.Random
import org.apache.commons.math3.random.{RandomDataGenerator, RandomGenerator, Well19937c}

/**
  * Created by novy on 10.04.16.
  */
class AgentActor(var state: RastriginSolution, island: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  def random = RandomComponent.randomData

  val mutationChance =  0.75
  val mutationRate = 0.1

  override def preStart(): Unit = {
    context.system.scheduler.schedule(10 seconds, 10 seconds, self, "migrate")
    context.system.scheduler.schedule(10 seconds, 20 second, self, "exchange result")
    context.system.scheduler.scheduleOnce(10 seconds, self, "transform")
    log.info(s"Got result ${state.evaluate()}")
  }

  override def receive: Receive = {
    case "migrate" =>
      island ! RequestMigration(state)

    case "exchange result" =>
      log.info(s"Exchanging result ${state.evaluate()}")
      island ! ExchangeResult(state)

    case "transform" =>
      state = transform(state)
  }

  def transform(solution: RastriginSolution): RastriginSolution =  mutateSolution(solution)

  def mutateSolution(s: RastriginSolution): RastriginSolution =
    if (Random.nextDouble() < mutationChance)
      RastriginSolution(s.result.map(f => if (Random.nextDouble() < mutationRate) mutateElement(f) else f))
    else
      s

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

  case class RastriginSolution(result: Array[Double]) {
    def evaluate(): Double =
      result.foldLeft(0.0)((sum, x) => sum + 10 + x * x - 10 * math.cos(2 * x * math.Pi))

    def betterThan(another: RastriginSolution): Boolean = this.evaluate() < another.evaluate()
  }

  def props(solution: RastriginSolution, island: ActorRef): Props =
  Props(new AgentActor(solution, island))

  case class RequestMigration(solution: RastriginSolution)

  case class ExchangeResult(agentState: RastriginSolution)

}

object RandomComponent {
  def randomGeneratorFactory(seed: Long): RandomGenerator = new Well19937c(seed)
  lazy val randomData = new RandomDataGenerator(randomGeneratorFactory(System.currentTimeMillis()))
}
