package com.jlab.demo

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

/**
 * Created by scorpiovn on 11/30/14.
 */
object IOManager {

  val hadoopConfig = new Configuration()
  val hdfs = FileSystem.get(hadoopConfig)

  def delete(path: String): Unit = {
    if(hdfs.exists(new Path(path))) {
      hdfs.delete(new Path(path), true) // delete file, true for recursive
    }
  }

  def merge(srcDir: String, dstFile: String): Unit = {
    FileUtil.copyMerge(hdfs, new Path(srcDir), hdfs, new Path(dstFile), false, hadoopConfig, null)
  }
}
