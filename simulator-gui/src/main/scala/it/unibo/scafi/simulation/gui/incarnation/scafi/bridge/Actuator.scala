package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.configuration.SensorName.output1
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.gui.model.space.Point3D

/**
  * describe a scafi actuator used to change world state
  * by export produced
  * @tparam OUT the type of value read by actuator
  */
trait Actuator[OUT] extends PartialFunction[EXPORT, (scafiWorld.ID => Unit)] {
  /**
    * put the sensor name where actuator change his state
    * @param name the name of actuator
    */
  def sensorName_=(name : String)

  /**
    * convert any val in the output value
    * @return none if the actuator can convert a specific value some of option otherwise
    */
  def valueParser : (Any) => (Option[OUT])

  /**
    * put the action used to parse any val
    * @param function the function used to parse any vale
    */
  def valueParser_= (function : (Any) => (Option[OUT]))

  /**
    * partial function : actuator is defined only when value parser
    * return Some value
    * @param x the export to parse
    * @return true if value parser return Some false otherwise
    */
  override def isDefinedAt(x: EXPORT): Boolean = valueParser(x.root()).isDefined
}
/**
  * describe action to actuate to the world, by export produced
  */
object Actuator {
  private val w = scafiWorld
  /**
    * an actuator that put export converted into sensor specified
    */
  val generalActuator : Actuator[Any] = new Actuator[Any] {
    private var name = output1
    private var action: (Any) => (Option[Any]) = v => Some(v)

    override def sensorName_=(name: String): Unit = this.name = name

    override def valueParser_=(function: Any => Option[Any]): Unit = this.action = function

    override def valueParser: Any => Option[Any] = action

    override def apply(export : EXPORT): Int => Unit = (id : w.ID) => w(id) match {
      case Some(node) => node.getDevice(name) match {
        case Some(dev) => if (dev.value != export.root()) w.changeSensorValue(id, output1, valueParser(export.root()).get)
        case _ =>
      }
      case _ =>
    }
  }
  /**
    * an actuator that move node by export produced
    */
  val movementActuator : Actuator[(Double,Double)] = new Actuator[(Double, Double)] {
    private var name = output1
    private var action : (Any) => (Option[(Double,Double)]) = v => Some(v.asInstanceOf[(Double,Double)])
    override def sensorName_=(name: String): Unit = this.name = name
    override def valueParser_=(function: Any => Option[(Double, Double)]): Unit = this.action = action
    override def valueParser: Any => Option[(Double, Double)] = action
    override def apply(export: EXPORT): Int => Unit = (id : w.ID) => {
      val (x,y) = action(export.root()).get
      val oldPos = w(id).get.position
      val point = Point3D(oldPos.x + x,oldPos.y + y,oldPos.z)
      w.moveNode(id,point)
    }
  }
}