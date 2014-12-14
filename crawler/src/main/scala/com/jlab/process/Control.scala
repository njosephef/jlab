package com.jlab.process

import configuration.Config


/**
 * Created by scorpiovn on 12/13/14.
 */
trait Control {
  def addCounter(s: Int): Int

  def subCounter(s: Int): Int

  def getCounter(): Int
}

class PipelineControl(config: Config) extends Control {
  val counter = new java.util.concurrent.atomic.AtomicInteger(0)

  def addCounter(n: Int): Int = {
    counter.getAndAdd(n)
  }

  def subCounter(n: Int): Int = {
    counter.getAndAdd(-n)
  }

  def getCounter(): Int = {
    counter.get()
  }
}
