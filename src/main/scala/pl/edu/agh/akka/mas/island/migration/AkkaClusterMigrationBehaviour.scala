package pl.edu.agh.akka.mas.island.migration

import akka.actor.ActorRef
import pl.edu.agh.akka.mas.island.migration.MigrationArena.{Agent, PerformMigration}

import scala.util.Random

/**
  * Created by novy on 11.06.16.
  */
class AkkaClusterMigrationBehaviour(migrationArena: ActorRef) extends MigrationBehaviour {
  override def chooseAgentsToMigrate(agents: List[Agent]): List[Agent] = randomAgents(2)(agents)

  override def migrateAgents(agentsToMigrate: List[Agent]): Unit = migrationArena ! PerformMigration(agentsToMigrate)

  private def randomAgents(numberOfAgents: Int): List[Agent] => List[Agent] = {
    agents => Random.shuffle(agents).take(numberOfAgents)
  }
}

object AkkaClusterMigrationBehaviour {
  def apply(migrationArena: ActorRef) = new AkkaClusterMigrationBehaviour(migrationArena)
}
