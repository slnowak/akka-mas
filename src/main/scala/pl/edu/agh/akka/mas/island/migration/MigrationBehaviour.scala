package pl.edu.agh.akka.mas.island.migration

import MigrationArena.Agent

/**
  * Created by novy on 11.06.16.
  */
trait MigrationBehaviour {
  def chooseAgentsToMigrate(agents: List[Agent]): List[Agent]

  def migrateAgents(agentsToMigrate: List[Agent])
}
