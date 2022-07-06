package com.herminiogarcia.dmaog

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator

import java.io.File

trait ClassGenerator {

  val rules: String

  def generateClasses(static: Boolean = false): Unit = {
    removeDirContent()
    new File("./tmp").mkdir() //create temp directory for tests
    new CodeGenerator(Option(rules), "shexml", "./tmp/", "com.example",
      None, None, None, None, static).generate()
  }

  def removeDirContent(): Unit = {
    new File("./tmp").deleteOnExit()
  }

  def loadClass(entityName: String): String = {
    val buffer = scala.io.Source.fromFile("./tmp/"+ entityName + ".java")
    val result = buffer.mkString
    buffer.close()
    result
  }

}
