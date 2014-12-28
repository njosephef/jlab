package com.jlab.fetch

import akka.actor._
import org.apache.http._
import org.apache.http.conn.scheme._
import org.apache.http.impl.conn.tsccm._
import org.apache.http.impl.client._
import org.apache.http.params._
import org.apache.http.conn.params._
import org.apache.http.client.methods._
import org.apache.http.client.params._
import org.apache.http.util._
import org.apache.http.protocol._

class HttpManager(config: Config, global: Config) {
  val http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory())
  val ssf = DummySSLScheme.getDummySSLScheme()
  val https = new Scheme("https", 443, ssf)

  val sr = new SchemeRegistry()
  sr.register(http)
  sr.register(https)

  val cm = new ThreadSafeClientConnManager(sr)
  cm.setMaxTotal(config.getInt("max_connection"))
  cm.setDefaultMaxPerRoute(config.getInt("max_connection_per_route"))

  val client = new DefaultHttpClient(cm)
  client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES)

  if (config.exists("user_agent")) {
    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, config("user_agent"))
  }

  val proxys = if (global.exists("proxy_host")) {
    val proxy = if (global.exists("proxy_port")) new HttpHost(global("proxy_host"), global("proxy_port").toInt)
                else new HttpHost(global("proxy_host"))

    client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy)
    Some(global("proxy_host"))
  } else None

  val min_interval_ms = config.getInt("min_interval_ms")

  def getClient() = {
    client
  }

  def getMinInterval() = {
    min_interval_ms
  }

  def getProxys() = proxys
}

class HttpFetcher(config: Config,
                  producer: ActorRef,
                  reseller: ActorRef,
                  manager: HttpManager
                   ) extends genericProcessor[Data, Data](config.getInt("threshold_in"), config.getInt("threshold_out"), producer, reseller, config.getLongOption("timeout_ms")) {

  val client = manager.getClient
  val interval = manager.getMinInterval
  var last_fetch_ms = 0L
  val hostname = java.net.InetAddress.getLocalHost.getHostName

  def fetch(url: String, proxy: Option[(String, Int)], headers: List[(String, String)]) = {
    Thread.sleep(interval)
    val startTime = Time.convertoMilisecond

    proxy match {
      case Some((h, p)) => client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(h, p))
      case None => ()
    }

    val get = new HttpGet(url)
    headers.foreach(kv => get.setHeader(kv._1, kv._2))
    val ctx = new BasicHttpContext() // Can this be re-used?
    val response = client.execute(get, ctx)

    val currentReq = ctx.getAttribute(ExecutionContext.HTTP_REQUEST).asInstanceOf[HttpUriRequest]
    val currentHost = ctx.getAttribute(ExecutionContext.HTTP_TARGET_HOST).asInstanceOf[HttpHost]
    val currentUrl = if (currentReq.getURI().isAbsolute()) currentReq.getURI().toString()
                     else (currentHost.toURI() + currentReq.getURI())

    val redirect = if (currentUrl != url) Some(currentUrl) else None
    val entity = response.getEntity()
    val page = EntityUtils.toByteArray(entity)

    println(">>>>>>> " + page)

    val status = response.getStatusLine()
    val code = status.getStatusCode()
    EntityUtils.consume(entity)

    val endTime = Time.convertoMilisecond
    val lat = endTime - startTime

    (status, code, page, lat, redirect)
  }

  def process(data: Data): Data = {

    val out = List[(String, String)](
//      ("fetch_version", Version.versionString),
//      ("fetch_format_version", Version.formatVersionString),
      ("fetch_host", hostname)
    )

    val urls = if (data exists "fetch_url") data("fetch_url") :: Nil else data("fetch_urls").split(" ").toList

    val compress = data.getOption("fetch_compress") match {
      case None | Some("true") => true
      case _ => false
    }

//    data + ("fetch_compress", "none")

    log.info("data information " + data.toJson())

    val proxy = if (data exists "fetch_proxy_host")
                  Some((data("fetch_proxy_host"), if (data exists "fetch_proxy_port") data("fetch_proxy_port").toInt else 80))
                else None

    //log.info("Starting fetch for "+urls+(manager.getProxys match { case Some(s)=> " (proxy "+s+")"; case None => "" }))

    val headers = if (data exists "fetch_headers") data.unwrapArray("fetch_headers").map(o => (o("field"), o("value"))) else Nil
    val ress = urls.view.zipWithIndex.map { case (url, i) =>

      val res = try {
        log.info(" Fetching urls")
        val (status, code, page, latency, redirect) = fetch(url, proxy, headers)

        log.info("compress status " + compress)
        log.info("compress status " + data.getOption("fetch_compress"))

        /*val zpage = if (compress) Encoding.byteToZippedString64(page) else new String(page)*/
        val zpage = new String(page)

//        val retcode = code.toString

        val o0 =
          ("fetch_time", Time.convertoSecond.toString) ::
          ("fetch_latency", latency.toString) ::
          ("fetch_size", page.length.toString) ::
          ("fetch_status_code", code.toString) ::
          ("fetch_status_line", status.toString) ::
          ("fetch_error", "false") ::
          Nil

        val o = redirect match {
          case Some(u) => ("fetch_redirect", u) :: o0
          case None => o0
        }

        if (data exists "fetch_proxy_host") log.info("Using proxy " + proxy + ")")
        log.info("Fetching " + url + ", status code " + code.toString)

        manager.getProxys match {
          case Some(s) => log.info("         Proxy " + s);
          case None => ()
        }

        log.info("         Latency " + latency)

        if (code >= 200 && code < 300) {
          /*("fetch_compress", if (compress) "zip64" else "none") ::("fetch_data", zpage) :: o*/
//          ("fetch_compress", "none") ::("fetch_data", zpage) :: o
          ("fetch_data", zpage) :: o
        }
        else o
      }
      catch {
        case e: Exception => ("fetch_error", "true") ::("fetch_error_reason", "exception") :: Nil
      }

      if (i == 0) res else res.map(kv => (kv._1 + "_" + i, kv._2))
    }

    val out1 = ress.foldLeft(out)((acc, v) => v ++ acc)
    (data - "fetch_compress") ++ out1
  }
}

object HttpFetcher {
  var iid = 0;
}
