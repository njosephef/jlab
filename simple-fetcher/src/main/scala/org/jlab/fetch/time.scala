package com.jlab.fetch

object Time {

  def convertoSecond(): Long = {
    convertoMilisecond()/1000
  }

  def convertoMilisecond(): Long = {
    (new java.util.Date()).getTime()
  }
}
