package org.jlab.fetch

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * Created by scorpiovn on 1/31/15.
 * Checks that properties mentioned as a part of application.json
 * are read properly.
 */
@RunWith(classOf[JUnitRunner])
class ConfigurationTester extends FunSuite {
  test("check that supplier values are being read properly") {
    val conf = ConfigFactory.load("application.json")
    println(conf.hasPath("pipelines"))
//    assert("""2""" === conf.hasPath("pipelines.httpManager.max_connection"))
//    assert(50 === conf.hasPath("pipelines.prefetcher.batch_size"))
  }
}
