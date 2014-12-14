package FileIO

import com.jlab.common.genericBatchProducer
import com.jlab.process.{Data, Control}
import configuration.Config

/**
 * Created by scorpiovn on 12/13/14.
 */
class FileBatchPrefetcher(config: Config, control: Control)
  extends genericBatchProducer[Data](config.getInt("batch_size"),
    config.getInt("threshold_in_batches"),
    config.getLongOption("timeout_ms"), control) {

  val file = config("file_name")

  log.info("Opening " + file)
  val data = scala.io.Source.fromFile(file).getLines.toArray
  var index = 0
  var batch = 0

  override def getBatch(sz: Int): Option[List[Data]] = {
    if (index > data.size) {
      return None
    }
    index += sz
    batch += 1
    val l = data.slice(index - sz, index).toList
    log.info("Fetched " + l.length.toString + " entrie(s) from " + file)
    val d = Data.empty
    Some(l map (e => d ++ List(("fetch_url", e), ("batch", batch.toString))))
  }
}
