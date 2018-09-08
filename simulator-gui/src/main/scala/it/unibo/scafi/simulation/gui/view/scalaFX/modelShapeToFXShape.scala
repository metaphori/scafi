package it.unibo.scafi.simulation.gui.view.scalaFX

import javafx.scene.shape._

import it.unibo.scafi.simulation.gui.model.core.{Shape => ModelShape}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.space.Point
import it.unibo.scafi.simulation.gui.view.ViewSetting._

import scalafx.Includes._
import scalafx.geometry.{Point2D => FXPoint}

/**
  * a startegy used to convert model shape into scalafx shape
  */
private[scalaFX] object modelShapeToFXShape {
  def apply(shape: Option[ModelShape], position : Point): Shape = {
    val p : FXPoint = position
    shape match {
      case Some(InternalRectangle(w,h,_)) => new Rectangle {
        this.x = p.x - w / 2
        this.y = p.y - h / 2
        this.width = w
        this.height = h
        this.fill = nodeColor
      }

      case Some(InternalCircle(r,_)) => new Circle {
        this.centerX = p.x
        this.centerY = p.y
        this.radius = r
        this.smooth = false
        this.fill = nodeColor
      }

      case Some(InternalPolygon(_,polyPoints @ _*)) => new Polygon {
        polyPoints.foreach {internalPoint => this.points.addAll(internalPoint.x + p.x, p.y + internalPoint.y)}
        this.stroke = nodeColor
      }

      case _ => new Rectangle {
        this.x = p.x
        this.y = p.y
        this.width = 10
        this.height = 10
        this.fill = nodeColor
      }
    }
  }
}
