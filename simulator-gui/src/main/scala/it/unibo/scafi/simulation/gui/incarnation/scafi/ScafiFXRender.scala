package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.controller.Presenter
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodesMoved
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesRemoved
import it.unibo.scafi.simulation.gui.model.simulation.BasicPlatform
import it.unibo.scafi.simulation.gui.pattern.observer.Source
import it.unibo.scafi.simulation.gui.view.scalaFX.FXSimulationPane
import it.unibo.scafi.space.Point3D

import scala.collection.mutable
import scalafx.application.Platform

/**
  * the render of output to a graphics view (scalafx library)
  * @param world to show
  * @param out where render show thing
  * @param contract the scafi simulation
  * @param neighbourRender a boolean value to define if show the neighbours or not
  */
//TODO PROBLEM WITH NEIGHBOURS, SOLVE
class ScafiFXRender(val world : ScafiLikeWorld,
                    val out : FXSimulationPane,
                    val contract : ScafiSimulationContract[ScafiLikeWorld,ScafiPrototype],
                    val neighbourRender : Boolean) extends Presenter[BasicPlatform with Source]{

  val removed = world.createObserver(Set(NodesRemoved))
  val moved = world.createObserver(Set(NodesMoved))
  private var prevNeighbour : mutable.Map[ScafiWorldIncarnation.ID,Iterable[ScafiWorldIncarnation.ID]] = mutable.Map()
  contract.getSimulation.get.ids.foreach {x => prevNeighbour += x -> contract.getSimulation.get.neighbourhood(x)}
  world <-- removed <-- moved
  override type OUTPUT = FXSimulationPane
  //RENDER COMPONENT DECIDE WHAT RENDER TO OUTPUT AND HOW
  override def onTick(float: Float): Unit = {
    val nodesRemoved = removed.nodeChanged()
    if (!nodesRemoved.isEmpty) {
      LogManager.log("view erasing..", LogManager.Middle)
      Platform.runLater{
        out.removeNode(nodesRemoved)
      }
    }
    //MOVING
    val nodesMoved = moved.nodeChanged()
    if (!nodesMoved.isEmpty) {
      LogManager.log("view moving..", LogManager.Middle)
      Platform.runLater{out.outNode(world(nodesMoved))}
      nodesMoved foreach { x =>
        val node = world(x).get
        contract.getSimulation.get.setPosition((x), Point3D(node.position.x, node.position.y, node.position.z))
      }
      nodesMoved foreach { x =>
        val node = world(x).get
        if (this.neighbourRender) {
          val oldIds = this.prevNeighbour(x).toSet
          val newIds = contract.getSimulation.get.neighbourhood(x)
          val id: world.ID = world(x).get.id
          val ids: Set[world.ID] = oldIds -- newIds
          val toAdd = newIds -- oldIds
          toAdd foreach {x => {this.prevNeighbour += x -> contract.getSimulation.get.neighbourhood(x)}}
          this.prevNeighbour += node.id -> newIds
          if (!ids.isEmpty) {
            Platform.runLater {
              out.removeNeighbour(id, ids)
            }
          }
          if (!(newIds -- oldIds).isEmpty) {
            Platform.runLater{
              out.outNeighbour(world(x).get, world.apply(toAdd))
            }
          }
        }
      }
    }
  }
}
