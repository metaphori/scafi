package it.unibo.rl.scafi
import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

object GradientProgram extends AggregateProgram with Gradients with StandardSensors {

  def isSource = sense[Boolean]("source")

  override def main(): Double = classicGradient(isSource)
}


object GradientWithRLMain extends App {

  val net = simulatorFactory.gridLike(GridSettings(16, 6, stepx = 1, stepy = 1, tolerance = 0.5), rng = 1.1)

  net.addSensor(name = "source", value = false)
  net.chgSensorValue(name = "source", ids = Set(3), value = true)

  val initial = java.lang.System.currentTimeMillis()
  var v = initial

  net.executeMany(
    node = GradientProgram,//new HopGradient("source"),
    size = 1000000,
    action = (n,i) => {
      val currT = java.lang.System.currentTimeMillis()
      val fromBeginning = currT - initial
      val fromLast = currT - v
      v = currT
      if (v % 2500 == 0) {
        println(net)
        println(s"From last print: ${fromLast}ms. From beginning: ${fromBeginning}")
      } else if(fromBeginning > 5000){
        net.chgSensorValue(name = "source", ids = Set(3), value = false)
      }
    })
}