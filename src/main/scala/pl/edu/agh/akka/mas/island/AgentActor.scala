package pl.edu.agh.akka.mas.island

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pl.edu.agh.akka.mas.island.AgentActor.{ExchangeResult, JoinArena}
import pl.edu.agh.akka.mas.island.MigrationArena.AgentState

import scala.concurrent.duration._

/**
  * Created by novy on 10.04.16.
  */
class AgentActor(state: AgentState, migrationArena: ActorRef, resultExchangeArena: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  override def preStart(): Unit = {
    context.system.scheduler.schedule(10 seconds, 10 seconds, self, "migrate")
    context.system.scheduler.schedule(10 seconds, 20 second, self, "exchange result")
  }

  override def receive: Receive = {
    case "migrate" =>
      migrationArena ! JoinArena(state)

    case "exchange result" =>
      resultExchangeArena ! ExchangeResult(state)
  }
}

object AgentActor {
  def props(agentState: AgentState, migrationArena: ActorRef, resultExchangeArena: ActorRef): Props =
    Props(new AgentActor(agentState, migrationArena, resultExchangeArena))

  case class JoinArena(agentState: AgentState)

  case class ExchangeResult(agentState: AgentState)

}
