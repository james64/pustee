package com.github.james64.pustee

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ReplProcessTest extends FunSuite {

  test("try it") {
    val (intro, repl) = ReplProcess.start("scala", "scala> ", Some("/home/dubovsky/tmp/scalalog"))

    println(intro)
    println(repl.runInRepl("1+1"))
    println(repl.runInRepl("\u0004\n"))

    repl.destroy()
  }
}
