package com.herminiogarcia.dmaog

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator
import picocli.CommandLine
import picocli.CommandLine.{Command, Option}

import java.io.{File, PrintWriter}
import java.util.concurrent.Callable


object Main {

  def main(args: Array[String]): Unit = {
    System.exit(new CommandLine(new Main()).execute(args: _*))
  }

}

@Command(name = "dmaog", version = Array("v0.1.0"),
  mixinStandardHelpOptions = true,
  description = Array("Generate data access objects and services from your mapping rules."))
class Main extends Callable[Int] {

  @Option(names = Array("-m", "--mapping"), required = true, description = Array("Path to the file with the mappings"))
  private var mappingRules: String = ""

  @Option(names = Array("-ml", "--mappingLanguage"), description = Array("Mapping language to use: ShExML or RML"))
  private var mappingLanguage: String = "ShExML"

  @Option(names = Array("-o", "--output"), required = true, description = Array("Path where to generate the output files"))
  private var outputPath: String = ""

  @Option(names = Array("-p", "--package"), required = true, description = Array("Package information for the generated files"))
  private var packageName: String = ""

  override def call(): Int = {
    val fileHandler = scala.io.Source.fromFile(mappingRules)
    try {
      val fileContent = fileHandler.mkString
      new CodeGenerator(fileContent, mappingLanguage, outputPath, packageName).generate()
      1 // well finished
    } finally { fileHandler.close() }
  }
}
