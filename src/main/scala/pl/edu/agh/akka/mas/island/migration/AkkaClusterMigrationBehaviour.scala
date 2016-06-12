package pl.edu.agh.akka.mas.island.migration

import akka.actor.ActorRef
import pl.edu.agh.akka.mas.island.migration.MigrationArena.{Agent, PerformMigration}

import scala.util.Random

/**
  * Created by novy on 11.06.16.
  */
class AkkaClusterMigrationBehaviour(migrationArena: ActorRef, agentsToMigrate: Int) extends MigrationBehaviour {
  override def chooseAgentsToMigrate(agents: List[Agent]): List[Agent] = randomAgents(agentsToMigrate)(agents)

  override def migrateAgents(agentsToMigrate: List[Agent]): Unit = {
    migrationArena ! PerformMigration(agentsToMigrate)
  }

  private def randomAgents(numberOfAgents: Int): List[Agent] => List[Agent] = {
    agents => Random.shuffle(agents).take(numberOfAgents)
  }
}

object AkkaClusterMigrationBehaviour {
  private def AGENTS_TO_MIGRATE_AT_ONCE = 2

  def apply(migrationArena: ActorRef,
            agentsToMigrate: Int = AGENTS_TO_MIGRATE_AT_ONCE) = {
    new AkkaClusterMigrationBehaviour(migrationArena, agentsToMigrate)
  }
}
