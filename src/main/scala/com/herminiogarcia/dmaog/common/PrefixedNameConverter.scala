package com.herminiogarcia.dmaog.common

trait PrefixedNameConverter {

  protected def convertPrefixedName(prefixes: Map[String, String])(prefixedURI: String): String = {
    prefixes.find(p => prefixedURI.startsWith(p._2)) match {
      case Some(value) =>
        if (value._1.isEmpty)
          prefixedURI.replaceFirst(value._2, "")
        else
          value._1 + prefixedURI.replaceFirst(value._2, "").capitalize
      case None => prefixedURI
    }
  }

  protected def convertPrefixedNameToValue(prefixes: Map[String, String])(prefixedURI: String): String = {
    prefixes.find(p => prefixedURI.startsWith(p._2)) match {
      case Some(value) => prefixedURI.replaceFirst(value._2, "")
      case None => prefixedURI
    }
  }

}
