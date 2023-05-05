package com.herminiogarcia.dmaog.common

import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.resultset.RDFOutput
import org.jline.utils.InputStreamReader
import com.typesafe.scalalogging.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.{ByteArrayInputStream, File, PrintWriter}
import java.util.Date
import scala.util.{Failure, Success}

trait ModelLoader extends MappingRulesRunner {

  @volatile var updateInProgress = false
  @volatile var fileAccessInProgress = false

  private val logger = Logger[ModelLoader]

  protected def loadModel(pathToRDF: String, mappingRules: Option[String], mappingLanguage: Option[String],
                          reloadMinutes: Option[Long], username: Option[String], password: Option[String],
                          drivers: Option[String], sparqlEndpoint: Option[String], sparqlQueryLimit: Option[String] = None): Model = mappingRules match {
    case Some(rules) =>
      if(sparqlEndpoint.isDefined) throw new Exception("It is not possible to update triple store contents")
      if(!new File(pathToRDF).exists()) applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"), username, password, drivers, sparqlEndpoint, sparqlQueryLimit)
      else reloadMinutes match {
        case Some(minutes) =>
          val modifiedTime = new File(pathToRDF).lastModified()
          val currentTime = new Date().getTime
          val reloadMillis = minutes * 60 * 1000
          if((currentTime - modifiedTime) > reloadMillis) {
            applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"), username, password, drivers, sparqlEndpoint, sparqlQueryLimit)
          } else {
            loadExistingModel(pathToRDF, sparqlEndpoint, sparqlQueryLimit)
          }
        case None => loadExistingModel(pathToRDF, sparqlEndpoint, sparqlQueryLimit)
      }
    case None => loadExistingModel(pathToRDF, sparqlEndpoint, sparqlQueryLimit)
  }

  private def loadExistingModel(pathToRDF: String, sparqlEndpoint: Option[String], sparqlQueryLimit: Option[String]): Model = synchronized {
    org.apache.jena.query.ARQ.init()
    logger.info("Loading existing model")
    DataLoaderFactory.getDataLoader(pathToRDF, sparqlEndpoint, sparqlQueryLimit).load
  }

  private def applyMappingRules(pathToRDF: String, mappingRules: String, mappingLanguage: String,
                                username: Option[String], password: Option[String],
                                drivers: Option[String], sparqlEndpoint: Option[String], sparqlQueryLimit: Option[String]): Model = {
    org.apache.jena.query.ARQ.init()
    logger.info("Applying mapping rules")
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
    loadExistingModel(pathToRDF, sparqlEndpoint, sparqlQueryLimit)
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
  def getDataLoader(path: String, sparqlEndpoint: Option[String], sparqlQueryLimit: Option[String]): DataLoader = sparqlEndpoint match {
    case Some(endpoint) => SparqlDataLoader(endpoint, sparqlQueryLimit)
    case None => FileDataLoader(path)
  }
}

case class SparqlDataLoader(path: String, sparqlQueryLimit: Option[String]) extends DataLoader with ResourceLoader {
  private val logger = Logger[SparqlDataLoader]
  
  def load: Model = {
    logger.info("Loading model from SPARQL endpoint")
    val limitedLoad = sparqlQueryLimit.map(" LIMIT " + _).getOrElse("")
    val sparql = loadFromResources("getAllData.sparql") + limitedLoad
    val query = QueryFactory.create(sparql)
    logger.info("Executing SPARQL query")
    logger.debug(sparql)
    val queryExecution = QueryExecutionFactory.sparqlService(path, query)
    val model = queryExecution.execConstruct()
    queryExecution.close()
    model
  }
}

case class FileDataLoader(path: String) extends DataLoader {
  private val logger = Logger[FileDataLoader]
  
  def load: Model = {
    logger.info("Loading model from file")
    RDFDataMgr.loadModel(path)
  }
}
