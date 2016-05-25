package pl.edu.agh.akka.mas.island.rastrigin

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pl.edu.agh.akka.mas.island.MutationArena.ApplyNewFeature
import pl.edu.agh.akka.mas.island.rastrigin.AgentActor._

import scala.concurrent.duration._

/**
  * Created by novy on 10.04.16.
  */
class AgentActor(var feature: RastriginFeature,
                 problem: RastriginProblem,
                 island: ActorRef)
  extends Actor with ActorLogging {

  import context.dispatcher

  override def preStart(): Unit = {
    context.system.scheduler.schedule(10 seconds, 10 seconds, self, Calculate)
    context.system.scheduler.schedule(10 seconds, 20 second, self, Migrate)
    context.system.scheduler.schedule(10 seconds, 20 seconds, self, Mutate)
    context.system.scheduler.schedule(15 second, 20 seconds, self, Reproduce)
  }

  override def receive: Receive = handlePeriodicTasks() orElse updateFeatures()

  private def handlePeriodicTasks(): Receive = {
    case Calculate =>
      val solution: RastriginSolution = problem.evaluate(feature)
      log.info(s"Exchanging result $solution")
      island ! ExchangeResult(solution)

    case Migrate =>
      island ! RequestMigration(feature)

    case Mutate =>
      island ! RequestMutation

    case Reproduce =>
      island ! RequestReproduction(feature)
  }

  private def updateFeatures(): Receive = {
    case ApplyNewFeature(newFeature) =>
      this.feature = newFeature
  }
}

object AgentActor {
  def props(feature: RastriginFeature, problem: RastriginProblem, island: ActorRef): Props =
    Props(new AgentActor(feature, problem, island))

  sealed trait PeriodicTask

  case object Calculate extends PeriodicTask

  case object Migrate extends PeriodicTask

  case object Mutate extends PeriodicTask

  case object Reproduce extends PeriodicTask


  case class RequestMutation(feature: RastriginFeature)

  case class RequestMigration(feature: RastriginFeature)

  case class RequestReproduction(feature: RastriginFeature)

  case class ExchangeResult(solution: RastriginSolution)

}
