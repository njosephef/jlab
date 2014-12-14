package com.jlab.demo;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

public class JavaDemo {
    public static void main(String[] args) {

        String logFile = System.getenv("SPARK_HOME") + "/apps/README.md";

        JavaSparkContext sc = new JavaSparkContext("local", "Simple App",
                System.getenv("SPARK_HOME") + "/apps/spark-1.1.0-bin-hadoop2.4",
                new String[]{"target/core-1.0.0.jar"});

        JavaRDD<String> logData = sc.textFile(logFile).cache();

        long numAs = logData.filter(new Function<String, Boolean>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public Boolean call(String s) {
                return s.contains("a");
            }
        }).count();

        long numBs = logData.filter(new Function<String, Boolean>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public Boolean call(String s) {
                return s.contains("b");
            }
        }).count();

        System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
    }
}