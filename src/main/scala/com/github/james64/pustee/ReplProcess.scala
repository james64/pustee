package com.github.james64.pustee

import java.io._
import java.lang.ProcessBuilder.Redirect

import com.github.james64.pustee.TakeUntilWord.{NoWord, Taken}

trait ReplProcess {

  def runInRepl(code: String): String

  def destroy() : Unit
}

object ReplProcess {

  def start(systemCmd: String, prompt: String, stdErrFilePath: Option[String] = None): (String, ReplProcess) = {
    val procBuilder : ProcessBuilder = new ProcessBuilder(systemCmd)

    stdErrFilePath
      .map(new File(_))
      .map(Redirect.appendTo)
      .foreach(procBuilder.redirectError)

    // todo stderr > /dev/null if errLogFile is null

    // todo support custom env variables here

    val proc = procBuilder.start
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
