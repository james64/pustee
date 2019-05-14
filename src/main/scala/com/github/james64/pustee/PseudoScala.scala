package com.github.james64.pustee

import java.io.{BufferedInputStream, InputStreamReader, OutputStreamWriter}
import java.util.UUID

import better.files.File
import com.github.james64.pustee.TakeUntilWord.{NoWord, Taken}
import com.pty4j.PtyProcess

class PseudoScala {

  private val tmpFile = File.newTemporaryFile()

  private val prompt = UUID.randomUUID().toString.replace('-', '\n')

  private val pty = PtyProcess.exec(Array("/usr/bin/scala", s"-Dscala.repl.prompt='$prompt'"))
  private val ptyWriter = new OutputStreamWriter(pty.getOutputStream)

  private val ptyReader = new InputStreamReader(new BufferedInputStream(pty.getInputStream))
  private val ptyReadIter = Iterator.continually(ptyReader.read.toChar)
  private val promptWaiter = TakeUntilWord(addCarriegeReturn(prompt))

  promptWaiter.take(ptyReadIter) // skip intro into first prompt

  def run(code: String) : String = {
    tmpFile.overwrite(code)
    ptyWriter.write(s":paste ${tmpFile.canonicalPath}\n")
    ptyWriter.flush()
    promptWaiter.take(ptyReadIter) match {
      case NoWord(output) => saveRemoveTwoLines(output)
      case Taken(output) => saveRemoveTwoLines(output)
    }
  }

  def destroy() : Unit = {
    pty.destroy()
    tmpFile.delete()
  }

  private def saveRemoveTwoLines(s: String) : String = {
    val first = s.indexOf('\n')
    val second = s.indexOf('\n', first+1)
    s.substring(second+1)
  }

  private def addCarriegeReturn(s: String) : String = s.flatMap {
    case '\n' => "\r\n"
    case c => c.toString
  }
}
