package com.jlab.fetch

object Fureteur {

  def main(args: Array[String]) {

    // Register local configs
    LocalConfig.registerLocalConfigs()

    var config = None: Option[Config]

    try {
      args(0) match {
        case "run" =>
          config = Some(Config.getConfig(args(1)))
        case "load" =>
          config = Some(Config.fromJson(LocalConfig.prepareConfigString(scala.io.Source.fromFile(args(1)).mkString)))
        case "show" =>
          println(Config.dumpConfig(args(1)))
          return
        case "list" =>
          println("Available configs:")
          Config.showConfigs().map(kv => println(" " + kv._1 + "   # " + kv._2))
          return
      }

    } catch {
      case e: java.lang.ArrayIndexOutOfBoundsException =>
        println("usage: fureteur run <config name>    # Start execution using a local config")
        println("       fureteur load <config path>   # Start execution using the provided config file")
        println("       fureteur show <config name>   # Dump a local config to STDOUT")
        println("       fureteur list                 # Show available local config")
        return
    }

    val system = new System(config get)
    system.start()
  }

  def start(config: String) = {
    val system = new System(Config.fromJson(config))
    system.start()
  }

}

object LocalConfig {

  def prepareConfigString(s: String) = {
    s.replaceAll("--.+", "")
  }

  def registerLocalConfigs() = {
    localConfigs foreach (c => Config.registerConfig(prepareConfigString(c), c))
  }

  val c0 =
    """-- This is an example configuration file for fureteur
{
  "conf" : "f2f",                                -- Configuration name
  "description" : "File to file operation",      -- Description
  "usage" : "f2f <input file> <output file>",    -- Usage
  "instance" : "fureteur"                       -- Instance name
  "pipelines" : [                                -- Pipelines
    {
      "httpManager" : {                          -- The http connection manager
          "max_connection" : "2",
          "max_connection_per_route" : "2",
          "min_interval_ms" : "1000"
        },  

      "prefetcher" : { "class" : "fileBatchPrefetcher",     -- Prefetching from files
                       "file_name" : "fureteur_in",         -- Input file name
                       "batch_size" : "50",                 -- Batch size when retrieving items from files
                       "threshold_in_batches" : "3",        -- Threshold (expressed in number of bacthes)
                       "timeout_ms" : "1000"                -- Timeout in ms 
                    },
      "httpFetchers": [ { "threshold_in" : "10",            -- Input threshold
                          "threshold_out" : "50",           -- Output threshold
                          "timeout_ms" : "1000"             -- Output timeout
                        },
                        { "threshold_in" : "10",            -- Input threshold (for second fetcher)
                          "threshold_out" : "50",           -- Output threshold (for second fetcher)
                          "timeout_ms" : "1000"             -- Output timeout (for second fetcher)
                        }
                      ],
      "writeback" :{
                     "file_name" : "fureteur_out"           -- File name
                   }
    }
  ]
}
    """

  val localConfigs = List[String](c0)
}
