package com.herminiogarcia.dmaog.common

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.resultset.RDFOutput
import org.jline.utils.InputStreamReader

import scala.concurrent.ExecutionContext.Implicits.global
import java.io.{ByteArrayInputStream, File, PrintWriter}
import java.util.Date
import scala.util.{Failure, Success}

trait ModelLoader extends MappingRulesRunner {

  @volatile var updateInProgress = false
  @volatile var fileAccessInProgress = false

  protected def loadModel(pathToRDF: String, mappingRules: Option[String], mappingLanguage: Option[String],
                          reloadMinutes: Option[Long], username: Option[String], password: Option[String],
                          drivers: Option[String], sparqlEndpoint: Option[String]): Model = mappingRules match {
    case Some(rules) =>
      if(sparqlEndpoint.isDefined) throw new Exception("It is not possible to update triple store contents")
      if(!new File(pathToRDF).exists()) applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"), username, password, drivers, sparqlEndpoint)
      else reloadMinutes match {
        case Some(minutes) =>
          val modifiedTime = new File(pathToRDF).lastModified()
          val currentTime = new Date().getTime
          val reloadMillis = minutes * 60 * 1000
          if((currentTime - modifiedTime) > reloadMillis) {
            applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"), username, password, drivers, sparqlEndpoint)
          } else {
            loadExistingModel(pathToRDF, sparqlEndpoint)
          }
        case None => loadExistingModel(pathToRDF, sparqlEndpoint)
      }
    case None => loadExistingModel(pathToRDF, sparqlEndpoint)
  }

  private def loadExistingModel(pathToRDF: String, sparqlEndpoint: Option[String]): Model = synchronized {
    org.apache.jena.query.ARQ.init()
    DataLoaderFactory.getDataLoader(pathToRDF, sparqlEndpoint).load
  }

  private def applyMappingRules(pathToRDF: String, mappingRules: String, mappingLanguage: String,
                                username: Option[String], password: Option[String],
                                drivers: Option[String], sparqlEndpoint: Option[String]): Model = {
    org.apache.jena.query.ARQ.init()
    if(!updateInProgress) {
      val turtle = generateDataByMappingLanguage(mappingRules, mappingLanguage, username, password, drivers)
      updateInProgress = true
      turtle onComplete {
        case Success(result) =>
          synchronized {
            val fileWriter = new PrintWriter(new File(pathToRDF))
            fileWriter.write(result)
            fileWriter.close()
            updateInProgress = false
          }
        case Failure(exception) =>
          updateInProgress = false
          throw exception
      }
    }
    loadExistingModel(pathToRDF, sparqlEndpoint)
  }

  def parseRDFData(data: String): Model = {
    val model = ModelFactory.createDefaultModel()
    model.read(new ByteArrayInputStream(data.getBytes), null, "TTL")
  }

}

sealed trait DataLoader {
  val path: String

  def load: Model
}

object DataLoaderFactory {
  def getDataLoader(path: String, sparqlEndpoint: Option[String]): DataLoader = sparqlEndpoint match {
    case Some(endpoint) => SparqlDataLoader(endpoint)
    case None => FileDataLoader(path)
  }
}

case class SparqlDataLoader(path: String) extends DataLoader with ResourceLoader {
  def load: Model = {
    val sparql = loadFromResources("getAllData.sparql")
    val query = QueryFactory.create(sparql)
    val queryExecution = QueryExecutionFactory.sparqlService(path, query)
    queryExecution.execConstruct()
  }
}

case class FileDataLoader(path: String) extends DataLoader {
  def load: Model = {
    RDFDataMgr.loadModel(path)
  }
}
