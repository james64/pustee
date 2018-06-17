package com.github.james64.pustee

import jupyter.kernel.interpreter.Interpreter.NoValue
import jupyter.kernel.interpreter.{Interpreter, InterpreterKernel}
import jupyter.kernel.protocol.{ParsedMessage, ShellReply}
import jupyter.kernel.protocol.ShellReply.KernelInfo.LanguageInfo
import jupyter.kernel.server.{ServerApp, ServerAppOptions}

object PusteeApp {

  def main(args: Array[String]): Unit = {

    ServerApp(
      id = "random id",
      name = "Pustee name",
      language = "spark-scala",
      kernel = new InterpreterKernel {
        override def apply(): Interpreter = PusteeInterpreter
      },
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
                        ): Interpreter.Result = NoValue

  private var execCounter: Int = 0

  override def executionCount: Int = {
    execCounter = execCounter + 1
    execCounter
  }
}