package pl.edu.agh.akka.mas.cluster.management.topology

/**
  * Created by novy on 09.04.16.
  */
class RingTopology private(islands: Vector[Island]) extends IslandTopology {

  override def neighboursOf(island: Island): List[Island] = {
    List(left(island), right(island)).flatten.distinct filterNot (_ == island)
  }

  private def left(island: Island): Option[Island] = {
    val indexOf: Int = islands indexOf island
    if (indexOf == -1) None else islands.lift((indexOf + islands.size - 1) % islands.size)
  }

  private def right(island: Island): Option[Island] = {
    val indexOf: Int = islands indexOf island
    if (indexOf == -1) None else islands.lift((indexOf + 1) % islands.size)
  }

  override def withNew(island: Island): IslandTopology = new RingTopology(islands :+ island)

  override def withoutExisting(island: Island): IslandTopology = new RingTopology(islands filterNot (_ == island))
}

object RingTopology {
  def apply(): RingTopology = new RingTopology(Vector())
}
