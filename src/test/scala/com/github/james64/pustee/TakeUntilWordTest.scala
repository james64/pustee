package com.github.james64.pustee

import com.github.james64.pustee.TakeUntilWord.{NoWord, Taken}
import org.scalatest.FunSuite
import org.scalatest.prop.TableDrivenPropertyChecks

class TakeUntilWordTest extends FunSuite with TableDrivenPropertyChecks {

  test("Generate fail shifts") {
    assert {
      TakeUntilWord("ABABAC").failShift === Vector(0, 1, 2, 2, 2, 2)
    }
  }

  test("Fail on empty word") {
    intercept[IllegalArgumentException] {
      TakeUntilWord("")
    }
  }

  test("No match") {
    val data = Table[String, String](
      ("word", "input")
      , ("word", "")
      , ("word", "one two three")
      , ("word", "wor")
    )

    forAll(data) {
      (word, input) =>
        assert {
          TakeUntilWord(word).take(input.iterator) === NoWord(input)
        }
    }
  }

  test("Match successfull") {
    val data = Table[String, String, String, String](
      ("word", "input", "output", "rest")
      ,("a", "bab", "b", "b")
      ,("aac", "|aaacd", "|a", "d")
      ,("x", "xxx", "", "xx")
      ,("|", "x|", "x", "")
      ,("\u0001", "a\u0001b\u0001c", "a", "b\u0001c")
      ,("\n", "one\ntwo\nthree", "one", "two\nthree")
      ,("|12121212", "one|1212121212", "one", "12")
    )

    forAll(data) {
      (word, input, expectedOutput, expectedRest) => {
        val Taken(actualOutput, actualRestIter) = TakeUntilWord(word).take(input.iterator)
        assert {
          (expectedOutput, expectedRest) === (actualOutput, actualRestIter.mkString)
        }
      }
    }
  }
}
