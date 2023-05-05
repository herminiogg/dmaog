package com.herminiogarcia.dmaog.common

import scala.util.Try

class PrefixesResolver {

  val prefixCCAPI = "https://prefix.cc/reverse"

  def searchForPrefix(prefixes: Map[String, String], uri: String): String = {
    searchForPrefixInDeclaredNamespaces(prefixes, uri) match {
      case Some(prefix) => prefix
      case None =>
        searchInPrefixCC(uri).getOrElse(normalizeURIForFile(uri))
    }
  }

  private def searchForPrefixInDeclaredNamespaces(prefixes: Map[String, String], uri: String): Option[String] = {
    prefixes.find(p => uri.startsWith(p._2)).headOption.map(_._1)
  }

  private def searchInPrefixCC(uri: String): Option[String] = {
    //TO DO: iterate to find the best result and only select if format is csv
    val finalQueryURI = s"$prefixCCAPI?uri=$uri&format=csv"
    val result = Try(scala.io.Source.fromURL(finalQueryURI))
    val lineWithResult = result.toOption.flatMap(_.getLines().find(_.contains(uri)))
    val prefix = lineWithResult.map(_.split(",")(0))
    result.foreach(_.close())
    prefix
  }

  private def normalizeURIForFile(uri: String): String = {
    uri.replace("/", "%2F")
  }
}


