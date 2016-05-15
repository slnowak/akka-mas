package pl.edu.agh.akka.mas.problems

import akka.actor.{ActorRef, Props}
import pl.edu.agh.akka.mas.island.AgentActor
import pl.edu.agh.akka.mas.problems.RastriginAgent.RastriginSolution

import scala.concurrent.duration._

/**
  * Created by ania on 5/8/16.
  */
class RastriginAgent(state: RastriginSolution, migrationArena: ActorRef, resultExchangeArena: ActorRef) extends AgentActor(state, migrationArena, resultExchangeArena) {

  import context.dispatcher

  override def preStart(): Unit = {
    super.preStart()
    context.system.scheduler.scheduleOnce(10 seconds, self, "run")
  }

  override def receive: Receive = super.receive orElse handleRun

  def handleRun: Receive = {
    case "run" => run
  }

  def run: Unit = {
    log.info("I'm running...")
  }
}

object RastriginAgent {
  def props(agentState: RastriginSolution, migrationArena: ActorRef, resultExchangeArena: ActorRef): Props =
    Props(new RastriginAgent(agentState, migrationArena, resultExchangeArena))

  case class RastriginSolution(result: Float) {
    def betterThan(another: RastriginSolution): Boolean = result > another.result
  }

}


