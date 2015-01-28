package com.jlab.fetch

import scala.collection.mutable.Queue


// A mutable FIFO with optional alert callback
class FIFO[T](x: Option[(Int, Int => Unit)]) {

  val queue = new Queue[T]
  var requested = false
  var enabled = true
  val (th, f) = x match {
    case Some((i, f)) => (i, f)
    case None => (-1, ((x: Int) => Unit))
  }

  def length() = {
    queue.size
  }

  def push(element: T) = {
    queue += element
    requested = false
    check()
  }

  def pushn(list: List[T]) = {
    queue ++= list
    requested = false
    check()
  }

  def pop(): T = {
    check()
    val value = queue.dequeue
    check();
    value
  }

  def check(): Unit = {
    val size = queue.size
    if (enabled && !requested && size <= th) {
      f(size) 
      requested = true
    }
  }

  def setEnabled(isEnabled: Boolean): Unit = {
    enabled = isEnabled;
  }

  def isEmpty(): Boolean = {
    queue.isEmpty
  }

  def init() = {
    check()
  }
}