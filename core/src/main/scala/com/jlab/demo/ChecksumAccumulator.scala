package com.jlab.demo

/**
 * Created by scorpiovn on 11/25/14.
 */
class ChecksumAccumulator {
  private var sum = 0
  def add(b: Byte) { sum += b }
  def checksum(): Int =  ~(sum & 0xFF) + 1
}

// In file ChecksumAccumulator.scala
import scala.collection.mutable.Map
object ChecksumAccumulator {
  private val cache = Map[String, Int]()

  def calculate(s: String): Int =
    if (cache.contains(s)) {
      cache(s)
    }
    else {
      val acc = new ChecksumAccumulator
      for (c <- s)  {
        acc.add(c.toByte)
      }
      val  cs = acc.checksum()
      cache += (s -> cs)
      cs
    }
}
