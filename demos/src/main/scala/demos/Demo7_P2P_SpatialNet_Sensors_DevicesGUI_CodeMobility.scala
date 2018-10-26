/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package demos

/**
  * Demo 7
  * - Peer-to-peer system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  * - Code mobility
  */

import akka.actor.Props
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.Point2D
import examples.gui.p2p.{DevViewActor => P2PDevViewActor}
import it.unibo.scafi.distrib.actor.p2p.{SpatialPlatform => SpatialP2PActorPlatform}

object Demo7_Platform extends BasicAbstractActorIncarnation with SpatialP2PActorPlatform {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  val SourceSensorName: String = "source"

  class CodeMobilityDeviceActor(override val selfId: UID,
                                _aggregateExecutor: Option[ProgramContract],
                                _execScope: ExecScope)
    extends DeviceActor(selfId, _aggregateExecutor, _execScope) with WeakCodeMobilitySupportBehavior {

    override def updateProgram(nid: UID, program: ()=>Any): Unit = program() match {
      case ap: AggregateProgram => aggregateExecutor = Some(ap); lastExport = None
    }
    override def propagateProgramToNeighbors(program: () => Any): Unit = {
      nbrs.foreach { case (_, NbrInfo(_, _, mailboxOpt, _)) =>
        mailboxOpt.foreach(ref => ref ! MsgUpdateProgram(selfId, program))
      }
    }

    override def beforeJob(): Unit = {
      super.beforeJob()
      if (reliableNbrs.isDefined) {
        nbrs = nbrs ++ nbrs.filterNot(n => reliableNbrs.get.contains(n._1)).map {
          case (id, NbrInfo(idn, _, mailbox, path)) => id -> NbrInfo(idn, None, mailbox, path)
        }
      }
    }
  }
  object CodeMobilityDeviceActor {
    def props(selfId: UID, program: Option[ProgramContract], execStrategy: ExecScope): Props =
      Props(classOf[CodeMobilityDeviceActor], selfId, program, execStrategy)
  }

  val idleAggregateProgram = () => new AggregateProgram {
    override def main(): String = "IDLE"
  }
  val stillValueAggregateProgram = () => new AggregateProgram {
    override def main(): Int = 1
  }
  val hopGradientAggregateProgram = () => new AggregateProgram {
    override def main(): Double = rep(Double.PositiveInfinity) {
      hops => {
        mux(sense(SourceSensorName)) { 0.0 } { 1 + minHood(nbr { hops }) }
      }
    }
  }
  val increasingAggregateProgram = () => new AggregateProgram {
    override def main(): Int = rep(0)(_ + 1)
  }
  val neighborsCountAggregateProgram = () => new AggregateProgram {
    override def main(): Int = foldhoodPlus(0)(_ + _)(1)
  }
}

import demos.{Demo7_Platform => Platform}

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo7_AggregateProgram extends Platform.AggregateProgram {
  override def main(): String = "ready"
}

// STEP 3: DEFINE MAIN PROGRAMS
object Demo7_MainProgram extends Platform.CmdLineMain {
  override def refineSettings(s: Platform.Settings): Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devActorProps = (id, program, scope) => Some(Platform.CodeMobilityDeviceActor.props(id, program, scope)),
      devGuiActorProps = ref => Some(P2PDevViewActor.props(Platform, ref))
    ))
  }
  override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
    val devInRow = P2PDevViewActor.DevicesInRow
    dm.addSensorValue(Platform.LocationSensorName, Point2D(dm.selfId%devInRow,(dm.selfId/devInRow).floor))
    dm.addSensorValue(Platform.SourceSensorName, false)
    dm.start
  }
}
