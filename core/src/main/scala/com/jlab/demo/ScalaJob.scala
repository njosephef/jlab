package com.jlab.demo


import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by ngvtien on 11/18/2014.
 */
object ScalaJob {
  def main (args: Array[String]) {

    val out: String = "/home/scorpiovn/sparkout/out"
    val input: String = args(0)
    val file = args(1)
    val source = System.getenv ("HOME") + input
    val conf = new SparkConf()
      .setMaster("spark://scorpiovn:7077")
      .setAppName("SimpleJob")
      .setSparkHome(System.getenv("SPARK_HOME"))
      .setJars(Array(System.getenv ("HOME") + "/git/maven-example/core/target/core-1.0.0.jar"))

    val sc = new SparkContext(conf)

    val analysis = new Analysis(sc, source);

    IOManager.delete(out)
    IOManager.delete(file)

    analysis.process(out, "ed");
    //    analysis.process()
    sc.stop()

    IOManager.merge(out, file)
  }
}
