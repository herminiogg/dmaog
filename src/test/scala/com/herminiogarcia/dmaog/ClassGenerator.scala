package com.herminiogarcia.dmaog

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator

import java.io.File

trait ClassGenerator {

  val rules: String

  def generateClasses(): Unit = {
    new File("./tmp").mkdir() //create temp directory for tests
    new CodeGenerator(rules, "shexml", "./tmp/", "com.example",
      None, None, None).generate()
  }

  def loadClass(entityName: String): String = {
    val buffer = scala.io.Source.fromFile("./tmp/"+ entityName + ".java")
    val result = buffer.mkString
    buffer.close()
    result
  }

}
