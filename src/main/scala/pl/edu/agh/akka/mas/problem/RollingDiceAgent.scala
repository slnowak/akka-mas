package pl.edu.agh.akka.mas.problem

import akka.actor.{ActorRef, Props}
import pl.edu.agh.akka.mas.island.AgentActor
import pl.edu.agh.akka.mas.island.MigrationArena.AgentState
import pl.edu.agh.akka.mas.problem.RollingDiceAgent.RollOfTheDice

import scala.concurrent.duration._
import scala.util.Random._

/**
  * Created by ania on 4/16/16.
  */
class RollingDiceAgent(var state: AgentState, migrationArena: ActorRef, resultExchangeArena: ActorRef) extends AgentActor(state, migrationArena, resultExchangeArena) {

  import context.dispatcher

  override def preStart(): Unit = {
    super.preStart()
    context.system.scheduler.schedule(10 seconds, 20 seconds, self, "roll_dice")
  }

  var bestRoll: RollOfTheDice = state.asInstanceOf[RollOfTheDice]
  var currentRoll: RollOfTheDice = state.asInstanceOf[RollOfTheDice]


  private def checkIfBestRoll(result: RollOfTheDice): Unit = {
    if (result.value < bestRoll.value) {
      bestRoll = result
      state = bestRoll
    }
  }

  private def rollDice(): Unit = {
    currentRoll = RollOfTheDice(nextInt(6) + 1)
    checkIfBestRoll(currentRoll)
  }

  def handleChangeState: Receive = {
    case "roll dice" => rollDice()
  }

  def handleRequestWithRoll: Receive = {
    case roll: RollOfTheDice => checkIfBestRoll(roll)
  }

  override def receive = handleChangeState orElse handleRequestWithRoll orElse handleMigration

}

object RollingDiceAgent {
  def props(agentState: AgentState, migrationArena: ActorRef, resultExchangeArena: ActorRef): Props =
    Props(new RollingDiceAgent(agentState, migrationArena, resultExchangeArena))

  case class RollOfTheDice(value: Int) extends AgentState

}
