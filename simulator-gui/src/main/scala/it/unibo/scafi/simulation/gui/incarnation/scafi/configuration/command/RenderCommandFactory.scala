package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.{CommandArg, CommandArgDescription}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Fail

/**
  * allow to change render information with command created
  */
class RenderCommandFactory(implicit val scafiConfiguration: ScafiConfigurationBuilder) extends CommandFactory {
  import CommandFactory._
  import RenderCommandFactory._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  override val name: String = "render-configuration"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Neighbour,BooleanType,true,international(name,Neighbour),defaultValue = Some(scafiConfiguration.neighbourRender)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(Neighbour) match {
    case Some(value : Boolean) => easyResultCreation(() => scafiConfiguration.neighbourRender = value)
    case Some(_) => creationFailed(Fail(wrongTypeParameter(BooleanType,Neighbour)))
    case _ => creationFailed(Fail(wrongParameterName(Neighbour)))
  }
}

object RenderCommandFactory {
  val Neighbour = "neighbour"
}
