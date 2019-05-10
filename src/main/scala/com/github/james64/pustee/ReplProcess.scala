package com.github.james64.pustee

import java.io._
import java.lang.ProcessBuilder.Redirect

import com.github.james64.pustee.TakeUntilWord.{NoWord, Taken}
import com.pty4j.PtyProcess

trait ReplProcess {

  def runInRepl(code: String): String

  def destroy() : Unit
}

// todo get rid of this and use pty4j to run scala repl
object ReplProcess {

  def start(systemCmd: String, prompt: String, stdErrFilePath: Option[String] = None): (String, ReplProcess) = {
    val procBuilder : ProcessBuilder = new ProcessBuilder(systemCmd)

    stdErrFilePath
      .map(new File(_))
      .map(Redirect.appendTo)
      .foreach(procBuilder.redirectError)

    val proc = procBuilder.start

    if(stdErrFilePath.isEmpty) {
      dropStdErr(proc)
    }

    val cmdOutputReader = new InputStreamReader(new BufferedInputStream(proc.getInputStream))
    val cmdOutputIter = Iterator.continually(cmdOutputReader.read.toChar)
    val promptWaiter = TakeUntilWord(prompt)

    val replProcess = new HisAsOposedToMyReplProcess(
      proc = proc,
      userInputWriter =new OutputStreamWriter(proc.getOutputStream),
      procOutputIter = cmdOutputIter,
      promptWaiter = promptWaiter
    )

    promptWaiter.take(cmdOutputIter) match {
      case Taken(out) => (out, replProcess)
      case NoWord(out) => throw new Error(out)
    }
  }

  private def dropStdErr(p: Process): Unit = new Thread(() => {
    val es = p.getErrorStream
    while (es.read != -1) {}
  }).start()

  private class HisAsOposedToMyReplProcess(proc: Process,
                                           userInputWriter: OutputStreamWriter,
                                           procOutputIter: Iterator[Char],
                                           promptWaiter: TakeUntilWord) extends ReplProcess {

    override def runInRepl(code: String): String = {
      val wrappedCode =
        s"""
           |:paste
           |$code ;
           |\u0004
       """.stripMargin

      userInputWriter.write(wrappedCode)
      userInputWriter.flush()

      promptWaiter.take(procOutputIter) match {
        case Taken(o) => o
        case NoWord(o) => throw new Error(o)
      }
    }

    override def destroy(): Unit = proc.destroyForcibly()
  }
}

object Testtt {

  def main(args: Array[String]): Unit = {
    val pty = PtyProcess.exec(Array("/usr/bin/scala", "-Dscala.repl.prompt='adfadf'"))

    val cmdOutputReader = new InputStreamReader(new BufferedInputStream(pty.getInputStream))
    val cmdOutputIter = Iterator.continually(cmdOutputReader.read.toChar)
    val promptWaiter = TakeUntilWord("adfadf")
    val writer = new OutputStreamWriter(pty.getOutputStream)

    val code =
      """:paste
        |val a = 1
        |val b = 2
        |a+b
        |\u0004
      """.stripMargin

    println("one")
    println(promptWaiter.take(cmdOutputIter))

    println("two")
    writer.write(code)
    writer.flush()


    // todo code (wrapped by scala repl's comments) is printed back to output stream by pty. We need to skip it.
    println("three")
    println(promptWaiter.take(cmdOutputIter))

    println("last")

    pty.destroy()
  }
}