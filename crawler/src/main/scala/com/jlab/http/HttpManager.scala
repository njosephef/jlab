package com.jlab.http

import com.jlab.common.DummySSLScheme
import configuration.Config
import org.apache.http.HttpHost
import org.apache.http.client.params.{CookiePolicy, ClientPNames}
import org.apache.http.conn.params.ConnRoutePNames
import org.apache.http.conn.scheme.{SchemeRegistry, PlainSocketFactory, Scheme}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.CoreProtocolPNames

/**
 * Created by scorpiovn on 12/13/14.
 */

class HttpManager(config: Config, global: Config) {
  val http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
  val ssf = DummySSLScheme.getDummySSLScheme();
  val https = new Scheme("https", 443, ssf);
  val sr = new SchemeRegistry();

  sr.register(http);
  sr.register(https);

  val cm = new ThreadSafeClientConnManager(sr)
  cm.setMaxTotal(config.getInt("max_connection"));
  cm.setDefaultMaxPerRoute(config.getInt("max_connection_per_route"));
  val client = new DefaultHttpClient(cm);
  client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);

  if (config.exists("user_agent")) {
    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, config("user_agent"));
  }
  val proxys = if (global.exists("proxy_host")) {
    val proxy = if (global.exists("proxy_port")) new HttpHost(global("proxy_host"), global("proxy_port").toInt)
    else
      new HttpHost(global("proxy_host"))
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

