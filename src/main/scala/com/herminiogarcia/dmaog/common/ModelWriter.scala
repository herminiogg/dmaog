package com.herminiogarcia.dmaog.common

import org.apache.jena.rdf.model.Statement
import org.apache.jena.update.{UpdateExecutionFactory, UpdateFactory}
import com.herminiogarcia.dmaog.common.Util._

import scala.collection.JavaConverters.{asScalaIteratorConverter, mapAsScalaMapConverter}

object WriterFactory {
  def getWriter(pathToGenerate: String, sparqlEndpoint: Option[String]): Writer = sparqlEndpoint match {
    case Some(endpoint) => DataSparqlEndpointWriter(endpoint)
    case None => DataLocalFileWriter(pathToGenerate)
  }
}

sealed trait Writer {
  val pathToGenerate: String

  def write(result: String): String
}

case class DataLocalFileWriter(override val pathToGenerate: String, filename: String = "data.ttl") extends Writer {
  def write(result: String): String = {
    Util.writeFile(pathToGenerate, filename, result)
    getFinalPathToGenerate(pathToGenerate)
  }
}

case class DataSparqlEndpointWriter(override val pathToGenerate: String) extends Writer with ResourceLoader with ModelLoader {
  def write(result: String): String = {
    val model = parseRDFData(result)
    val statements = model.listStatements()
    val prefixes = model.getNsPrefixMap.asScala.toMap
    val sparql = convertTriplesToSPARQL(statements.asScala.toList, prefixes)
    runInsertSPARQL(sparql)
    pathToGenerate
  }

  private def convertTriplesToSPARQL(triples: List[Statement], prefixes: Map[String, String]): String = {
    val template = loadFromResources("insertData.sparql")

    val formattedTriples = triples.map(t => {
      convertURIToPrefixedValue(t.getSubject.getURI, prefixes) + " " +
        convertURIToPrefixedValue(t.getPredicate.getURI, prefixes) + " " + {
        if(t.getObject.isLiteral && t.getObject.asLiteral().getLanguage.nonEmpty)
          "\"" + t.getObject.asLiteral().getString + "\"" +"@" + t.getObject.asLiteral().getLanguage
        else if(t.getObject.isLiteral) "\"" + t.getLiteral.getString + "\"" +"^^<" + t.getLiteral.getDatatype.getURI + ">"
        else convertURIToPrefixedValue(t.getObject.asResource().getURI, prefixes) + " "
      } + " ."
    }).mkString("\n")

    val formattedPrefixes = prefixes.map { case(k, v) => {
      "PREFIX " + k + ":" + " <" + v + ">"
    }}.mkString("\n")

    template.replaceFirst("\\$triples", formattedTriples).replaceFirst("\\$prefixes", formattedPrefixes)
  }

  private def runInsertSPARQL(sparql: String) = {
    val request = UpdateFactory.create(sparql)
    UpdateExecutionFactory.createRemote(request, pathToGenerate).execute()
  }

  private def convertURIToPrefixedValue(uri: String, prefixes: Map[String, String]): String = {
    prefixes.find { case (_, v) => v.equals(uri) }
      .map { case (k, v) => k + ":" + uri.replaceFirst(v, "")}
      .getOrElse("<" + uri + ">")
  }
}