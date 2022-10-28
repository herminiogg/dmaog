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

@Command(name = "dmaog", version = Array("v0.1.2"),
  mixinStandardHelpOptions = true,
  description = Array("Generate data access objects and services from your mapping rules."))
class Main extends Callable[Int] {

  @Option(names = Array("-m", "--mapping"), description = Array("Path to the file with the mappings"))
  private var mappingRules: String = ""

  @Option(names = Array("--static"), description = Array("Exploit mapping rules without executing them"))
  private var staticMappingRulesExploitation: Boolean = false

  @Option(names = Array("-ml", "--mappingLanguage"), description = Array("Mapping language to use: ShExML or RML"))
  private var mappingLanguage: String = "ShExML"

  @Option(names = Array("-o", "--output"), required = true, description = Array("Path where to generate the output files"))
  private var outputPath: String = ""

  @Option(names = Array("-d", "--datafile"), description = Array("Path where the datafile is located if no mapping rules are provided."))
  private var datafile: String = null

  @Option(names = Array("-se", "--sparqlEndpoint"), description = Array("URL pointing to the SPARQL endpoint"))
  private var sparqlEndpoint: String = null

  @Option(names = Array("-seu", "--sparqlEndpointUsername"), description = Array("Username for the SPARQL endpoint"))
  private var sparqlEndpointUsername: String = null

  @Option(names = Array("-sep", "--sparqlEndpointPassword"), description = Array("Password for the SPARQL endpoint"))
  private var sparqlEndpointPassword: String = null

  @Option(names = Array("-p", "--package"), required = true, description = Array("Package information for the generated files"))
  private var packageName: String = ""

  @Option(names = Array("-u", "--username"), description = Array("Username in case of database connection"))
  private var username: String = null

  @Option(names = Array("-ps", "--password"), description = Array("Password in case of database connection"))
  private var password: String = null

  @Option(names = Array("-dr", "--drivers"), description = Array("Drivers string in case it is not included in ShExML"))
  private var drivers: String = null

  override def call(): Int = {
    val fileHandler = if(mappingRules.nonEmpty) scala.Option(scala.io.Source.fromFile(mappingRules)) else scala.Option.empty
    val fileContent = fileHandler.map(_.mkString)
    try {
      new CodeGenerator(fileContent, mappingLanguage, outputPath, packageName,
        scala.Option(username), scala.Option(password), scala.Option(drivers),
        scala.Option(sparqlEndpoint), scala.Option(sparqlEndpointUsername), 
        scala.Option(sparqlEndpointPassword), scala.Option(datafile), staticMappingRulesExploitation).generate()
      1 // well finished
    } finally { fileHandler.foreach(_.close()) }
  }
}
