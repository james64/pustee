package com.github.james64.pustee

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PseudoScalaTest extends FunSuite {

  test("try it") {
    val scala = new PseudoScala()

    Vector(
      "val a = 10",
      "def f : Int = a + 1",
      "f",
      "def g(i: Int) : Int = i*2",
      "g(10)",
      "f('a')",
      "f(\"ahoj\")"
    )
      .map(scala.run)
      .foreach(println)

    scala.destroy()
  }
}
