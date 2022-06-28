package com.herminiogarcia.dmaog.common

import java.io.{File, PrintWriter}

object Util {

  def writeFile(pathToGenerate: String, filename: String, content: String): Unit = {
    val file = new PrintWriter(new File(pathToGenerate + "/" + filename))
    file.write(content)
    file.close()
  }

  def isSparqlEndpoint(path: String): Boolean = {
    path.contains("http://") || path.contains("https://")
  }
}
