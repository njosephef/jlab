package FileIO

import com.jlab.common.genericBatchReseller
import com.jlab.process.{Control, Data}
import configuration.Config

/**
 * Created by scorpiovn on 12/13/14.
 */
class FileBatchWriteback(config: Config, control: Control) extends genericBatchReseller[Data](control) {

  //val log = Logging(context.system, this)
  val fname = config("file_name")
  val file = new java.io.FileWriter(fname)

  def resell(batch: List[Data]) = {
    log.info("Writing " + batch.length.toString + " entrie(s) to " + file)
    def doit(b: List[Data]): Unit = {
      b match {
        case x :: xs => {
          val s = x.toJson + "\n";
          log.info("Writing " + s + " contain to " + file)
          file.write(s);
          doit(xs)
        }
        case Nil =>
      }
    }
    doit(batch)
  }

}
