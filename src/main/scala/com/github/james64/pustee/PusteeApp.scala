package com.github.james64.pustee

import jupyter.kernel.interpreter.Interpreter.Value
import jupyter.kernel.interpreter.{DisplayData, Interpreter}
import jupyter.kernel.protocol.{ParsedMessage, ShellReply}
import jupyter.kernel.protocol.ShellReply.KernelInfo.LanguageInfo
import jupyter.kernel.server.{ServerApp, ServerAppOptions}

object PusteeApp {

  def main(args: Array[String]): Unit = {

    ServerApp(
      id = "random id",
      name = "Pustee name",
      language = "spark-scala",
      kernel = () => PusteeInterpreter,
      progPath = "/home/dubovsky/github/james64/pustee/build/libs/pustee-all.jar",
      isJar = true,
      options = ServerAppOptions(
        connectionFile = args(0)
      )
    )

    println("hmmmm")
  }
}

object PusteeInterpreter extends Interpreter {

  private var scalaRepl : PseudoScala = _

  override def init() : Unit = scalaRepl = new PseudoScala()
  override def initialized : Boolean = scalaRepl != null

  override def languageInfo: ShellReply.KernelInfo.LanguageInfo = LanguageInfo(
    name = "spark-scala",
    version = "0.0.1",
    mimetype = "",
    file_extension = ".scala",
    nbconvert_exporter = ""
  )

  override def interpret(line: String,
                         output: Option[(String => Unit, String => Unit)],
                         storeHistory: Boolean,
                         current: Option[ParsedMessage[_]]
                        ): Interpreter.Result = Value(Seq(DisplayData.text(scalaRepl.run(line))))

  private var execCounter: Int = 0

  override def executionCount: Int = {
    execCounter = execCounter + 1
    execCounter
  }
}