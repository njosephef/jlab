package org.jlab.model

/**
 * Created by scorpiovn on 12/30/14.
 */
/*case class Message(header: String, body: Array[Byte])

case class HTMLMessage(url: String, html: String)

case class TextMessage(title: String, body: String)*/
case class HTMLContent(url:String, html:String)

case class Message(msg: String)