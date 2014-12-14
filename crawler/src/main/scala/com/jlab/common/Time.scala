package com.jlab.common

/**
 * Created by scorpiovn on 12/13/14.
 */
object Time {
  def sNow() = { msNow/1000 }
  def msNow() ={ (new java.util.Date()).getTime() }
}
