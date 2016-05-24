package pl.edu.agh.akka.mas

import org.apache.commons.math3.random.{RandomDataGenerator, Well19937c}

/**
  * Created by novy on 24.05.16.
  */
object UglyStaticGlobalRandomGenerator {

  def defaultRandomGenerator(): RandomDataGenerator = new RandomDataGenerator(
    new Well19937c(System.currentTimeMillis())
  )
}
