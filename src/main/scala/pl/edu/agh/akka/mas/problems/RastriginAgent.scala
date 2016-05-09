package pl.edu.agh.akka.mas.problems

import akka.actor.{ActorRef, Props}
import pl.edu.agh.akka.mas.island.MigrationArena.AgentState
import pl.edu.agh.akka.mas.island.AgentActor

import scala.concurrent.duration._

/**
  * Created by ania on 5/8/16.
  */
class RastriginAgent(state: AgentState, migrationArena: ActorRef, resultExchangeArena: ActorRef) extends AgentActor(state, migrationArena, resultExchangeArena){

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
  def props(agentState: AgentState, migrationArena: ActorRef, resultExchangeArena: ActorRef): Props =
    Props(new RastriginAgent(agentState, migrationArena, resultExchangeArena))

  case class RastriginState(result: Float) extends AgentState[RastriginState] {
    override def betterThan(another: RastriginState): Boolean = result > another.result
  }
}


