package com.github.james64.pustee

import scala.annotation.tailrec

/**
  * Reads stream of characters until first occurence of WORD in it. When it is found
  *
  *
  * Returns both string of characters read until WORD
  * is reached (without trailing WORD) and iterator with the rest of stream.
  *
  * It uses Knutt-Moris-Pratt algorithm to find WORD in stream without need to ever going back in stream.
  */
case class TakeUntilWord(word: String) {

  import TakeUntilWord._

  require(word.nonEmpty, "Delimiting word must be non empty")

  /**
    * For every INDEX within WORD we want to find the length of longest PREFIX of WORD such that:
    *
    * 1) PREFIX ends before INDEX
    * 2) PREFIX is suffix of WORD.substr(0, INDEX)
    *
    * Let's call this sequence L(n) for 0 <= n < WORD.length
    *
    * L(n) can be computed inductively as follows:
    *
    * L(0) = 0
    * L(1) = 0
    * L(n) = if WORD.charAt( L(n-1) ) == WORD.charAt(n-1) then      L(n-1) + 1
    *                                                     otherwise 0
    *
    * When we fail at indexes 0 or 1 there is no prefix fulfilling two conditions above. We define L to be zero here.
    * For greater indexes there are two possibilities. Either we are able to make last longest (potentially empty)
    * prefix longer or we need to start over with empty prefix.
    */
  def prefixLengths : Stream[Int] = 0 #:: 0 #:: prefixLengths.zipWithIndex.tail.map {
    case (lastLn, lastIndex) => if(word(lastLn) == word(lastIndex)) lastLn + 1 else 0
  }

  /**
    * This tells us how much we want to shift WORD forward along input from iterator when difference is found
    * on position i in WORD. Definition of prefixLengths implies that
    *
    * - failShift(0) is always zero
    * - for i > 0 => failShift(i) is always non-zero
    */
  val failShift : Vector[Int] = prefixLengths
    .take(word.length)
    .toVector
    .zipWithIndex
    // subtract prefix length from failed match position to get final value of how much we can move WORD forward
    .map { case (len, n) => n - len }

  def take(iter: Iterator[Char]): Result = if(iter.isEmpty)
    NoWord("")
  else
    matchChar(iter.next(), iter, 0, StringBuilder.newBuilder)

  /**
    * This method would deserve to be split into more methods. But we want it to benefit from being tail recursive
    * so we keep it as is.
    */
  @tailrec
  private def matchChar(char: Char, rest: Iterator[Char], wordPos: Int, outputAcc: StringBuilder): Result =
    if(char == word(wordPos)) {
      if(wordPos + 1 == word.length)
        // whole WORD matched
        Taken(outputAcc.mkString, rest)
      else if(rest.isEmpty)
        // partial match of WORD and no further input
        NoWord(outputAcc.mkString ++ word.substring(0, wordPos+1))
      else
        // try to match next char of WORD
        matchChar(rest.next, rest, wordPos + 1, outputAcc)
    } else {
      val shift = failShift(wordPos)
      if(shift == 0) {
        if(rest.isEmpty)
          // no match and no further input
          NoWord(outputAcc.mkString :+ char)
        else
          // first letter of WORD does not match. Read next and start from scratch
          matchChar(rest.next, rest, 0, outputAcc + char)
      } else {
        // char does not match but there is common prefix. Move SHIFT positions back in WORD
        matchChar(char, rest, wordPos - shift, outputAcc ++= word.substring(0, shift))
      }
    }
}

object TakeUntilWord {

  sealed trait Result

  case class NoWord(output: String) extends Result
  case class Taken(output: String, rest: Iterator[Char]) extends Result
}