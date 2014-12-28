/**
 * Licensed to Gravity.com under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Gravity.com licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gravity.goose.utils

import com.gravity.goose.cleaners.{DocumentCleaner, StandardDocumentCleaner}
import com.gravity.goose.extractors.ContentExtractor
import com.gravity.goose.images.{UpgradedImageIExtractor, ImageExtractor, Image}
import com.gravity.goose.{Article, Configuration}
import org.apache.http.client.HttpClient

//import org.apache.http.client.HttpClient

//import org.apache.http.client.HttpClient
import org.jsoup.nodes.{Document, Element}
import org.jsoup.Jsoup
import java.io.File
import com.gravity.goose.outputformatters.{StandardOutputFormatter, OutputFormatter}

/**
 * Created by Jim Plush
 * User: jim
 * Date: 8/18/11
 */
class Filter(config: Configuration) {

  import Filter._

  def filter(url: String, rawHTML: String): Article = {
    val article = new Article()
    for {
//      parseCandidate <- URLHelper.getCleanedUrl(filterCandidate.url)
//      rawHtml <- getHTML(filterCandidate, parseCandidate)
      doc <- getDocument(url, rawHTML)
    } {
      trace("Crawling url: " + url)

      val extractor = getExtractor
      val docCleaner = getDocCleaner
      val outputFormatter = getOutputFormatter

      article.finalUrl = url
      article.rawHtml = rawHTML
      article.doc = doc
      article.rawDoc = doc.clone()

      article.title = extractor.getTitle(article)
      article.publishDate = config.publishDateExtractor.extract(doc)
      article.additionalData = config.getAdditionalDataExtractor.extract(doc)
      article.metaDescription = extractor.getMetaDescription(article)
      article.metaKeywords = extractor.getMetaKeywords(article)
      article.canonicalLink = extractor.getCanonicalLink(article)
      article.tags = extractor.extractTags(article)

      article.doc = docCleaner.clean(article)

      extractor.calculateBestNodeBasedOnClustering(article) match {
        case Some(node: Element) => {
          article.topNode = node
          article.movies = extractor.extractVideos(article.topNode)

          if (config.enableImageFetching) {
            trace(logPrefix + "Image fetching enabled...")
            val imageExtractor = getImageExtractor(article)
            try {
              if (article.rawDoc == null) {
                article.topImage = new Image
              } else {
                article.topImage = imageExtractor.getBestImage(article.rawDoc, article.topNode)
              }
            } catch {
              case e: Exception => {
                warn(e, e.toString)
              }
            }
          }
          article.topNode = extractor.postExtractionCleanup(article.topNode)

          article.cleanedArticleText = outputFormatter.getFormattedText(article.topNode)
        }
        case _ => trace("NO ARTICLE FOUND")
      }
      releaseResources(article)
      article
    }

    article
  }

  /*def getHTML(crawlCandidate: FilterCandidate, parsingCandidate: ParsingCandidate): Option[String] = {
    if (crawlCandidate.rawHTML != null) {
      Some(crawlCandidate.rawHTML)
    } else {
      config.getHtmlFetcher.getHtml(config, parsingCandidate.url.toString) match {
        case Some(html) => {
          Some(html)
        }
        case _ => None
      }
    }
  }*/


  def getImageExtractor(article: Article): ImageExtractor = {
    val httpClient: HttpClient = config.getHtmlFetcher.getHttpClient
    new UpgradedImageIExtractor(httpClient, article, config)
  }

  def getOutputFormatter: OutputFormatter = {
    StandardOutputFormatter
  }

  def getDocCleaner: DocumentCleaner = {
    new StandardDocumentCleaner
  }

  def getDocument(url: String, rawlHtml: String): Option[Document] = {

    try {
      Some(Jsoup.parse(rawlHtml))
    } catch {
      case e: Exception => {
        trace("Unable to parse " + url + " properly into JSoup Doc")
        None
      }
    }
  }

  def getExtractor: ContentExtractor = {
    config.contentExtractor
  }

  /**
  * cleans up any temp files we have laying around like temp images
  * removes any image in the temp dir that starts with the linkhash of the url we just parsed
  */
  def releaseResources(article: Article) {
    trace(logPrefix + "STARTING TO RELEASE ALL RESOURCES")

    val dir: File = new File(config.localStoragePath)

    dir.list.foreach(filename => {
      if (filename.startsWith(article.linkhash)) {
        val f: File = new File(dir.getAbsolutePath + "/" + filename)
        if (!f.delete) {
          warn("Unable to remove temp file: " + filename)
        }
      }
    })
  }

}

object Filter extends Logging {
  val logPrefix = "crawler: "
}