package org.jlab.domain

import scalaz._, Scalaz._
import argonaut._, Argonaut._

/**
 * Created by scorpiovn on 2/2/15.
 */
case class Pipelines(httpManager: Option[HttpManager], prefetcher: Option[Prefetcher], httpFetchers: Option[List[HttpFetcher]])
object Pipelines {
  implicit def PipelinesCodecJson: CodecJson[Pipelines] =
    casecodec3(Pipelines.apply, Pipelines.unapply)("httpManager", "prefetcher", "httpFetchers")
}

case class HttpManager(maxConnection: Int, maxConnnectionPerRoute: Int, interval: Int)
object HttpManager {
  implicit def HttpManagerCodecJson: CodecJson[HttpManager] =
    casecodec3(HttpManager.apply, HttpManager.unapply)("maxConnection", "maxConnectionPerRoute", "interval")
}

case class Prefetcher(batchSize: Int, threshold: Int, timeout: Int)
object Prefetcher {
  implicit def PrefetcherCodecJson: CodecJson[Prefetcher] =
    casecodec3(Prefetcher.apply, Prefetcher.unapply)("batchSize", "threshold", "timeout")
}

case class HttpFetcher(thresholdIn: Int, thresholdOut: Int, timeout: Int)
object HttpFetcher {
  implicit def HttpFetcherCodecJson: CodecJson[HttpFetcher] =
    casecodec3(HttpFetcher.apply, HttpFetcher.unapply)("thresholdIn", "thresholdOut", "timeout")
}
