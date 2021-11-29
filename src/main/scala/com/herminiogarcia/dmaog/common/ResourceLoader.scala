package com.herminiogarcia.dmaog.common

trait ResourceLoader {

  protected def loadFromResources(filePath: String): String = {
    val file = scala.io.Source.fromResource(filePath)
    val content = file.mkString
    file.close()
    content
  }

}
