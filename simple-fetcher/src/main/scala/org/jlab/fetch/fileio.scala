package com.jlab.fetch

import java.io.File

import org.apache.commons.io.FileUtils
import org.json4s.ShortTypeHints
import org.json4s.jackson.Serialization

case class Message(url:String, html:String)

// Taking URLs in batches from a file
class BatchPrefetcher(config: Config, control: Control)
  extends genericBatchProducer[Data](config.getInt("batch_size"),
    config.getInt("threshold_in_batches"),
    config.getLongOption("timeout_ms"), control) {

  val file = config("file_name")

  log.info("Opening " + file)
  val data = scala.io.Source.fromFile(file).getLines().toArray
  var index = 0
  var batch = 0

  override def getBatch(sz: Int): Option[List[Data]] = {
    if (index > data.size) {
      return None
    }
    index += sz
    batch += 1
    val listURL = data.slice(index - sz, index).toList
    log.info("Fetched " + listURL.length.toString + " entrie(s) from " + file)
    val d = Data.empty()
    Some(listURL map (e => d ++ List(("fetch_url", e), ("batch", batch.toString))))
  }
}


class BatchWriteback(config: Config, control: Control) extends genericBatchReseller[Data](control) {

  //val log = Logging(context.system, this)
  val fileName = config("file_name")

  def resell(batch: List[Data]) = {
    log.info("Writing " + batch.length.toString + " entrie(s) to " + fileName)
    def doit(b: List[Data]): Unit = {
      b match {
        case x :: xs => {
          // get just important information, fetch_url and fetch_data
          // then convert them to a json string
          implicit val formats = Serialization.formats(
            ShortTypeHints(
              List(
                classOf[Message]
              )
            )
          )

          val content = new Message(x("fetch_url"), x("fetch_data"))
          val json = Serialization.writePretty(content)
          FileUtils writeStringToFile(new File(fileName), json, "UTF-8")
          log.info(json)
//          val s = x.toJson + "\n"
//          FileUtils writeStringToFile(new File(fileName), s, "UTF-8")
          doit(xs)
        }
        case Nil =>
      }
    }
    doit(batch)
  }

}