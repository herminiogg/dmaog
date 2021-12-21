package com.herminiogarcia.dmaog.common

import com.herminiogarcia.com.herminiogarcia.dmaog.common.MappingRulesRunner
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.RDFDataMgr

import scala.concurrent.ExecutionContext.Implicits.global
import java.io.{File, FileWriter}
import java.util.Date
import scala.util.{Failure, Success}

trait ModelLoader extends MappingRulesRunner {

  @volatile var updateInProgress = false
  @volatile var fileAccessInProgress = false

  protected def loadModel(pathToRDF: String, mappingRules: Option[String], mappingLanguage: Option[String],
                          reloadMinutes: Option[Long], username: Option[String], password: Option[String], drivers: Option[String]): Model = mappingRules match {
    case Some(rules) =>
      if(!new File(pathToRDF).exists()) applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"), username, password, drivers)
      else reloadMinutes match {
        case Some(minutes) =>
          val modifiedTime = new File(pathToRDF).lastModified()
          val currentTime = new Date().getTime
          val reloadMillis = minutes * 60 * 1000
          if((currentTime - modifiedTime) > reloadMillis) {
            applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"), username, password, drivers)
          } else {
            loadExistingModel(pathToRDF)
          }
        case None => loadExistingModel(pathToRDF)
      }
    case None => loadExistingModel(pathToRDF)
  }

  private def loadExistingModel(pathToRDF: String): Model = synchronized {
    RDFDataMgr.loadModel(pathToRDF)
  }

  private def applyMappingRules(pathToRDF: String, mappingRules: String, mappingLanguage: String,
                                username: Option[String], password: Option[String], drivers: Option[String]): Model = {
    org.apache.jena.query.ARQ.init()
    if(!updateInProgress) {
      val turtle = generateDataByMappingLanguage(mappingRules, mappingLanguage, username, password, drivers)
      updateInProgress = true
      turtle onComplete {
        case Success(result) =>
          synchronized {
            val fileWriter = new FileWriter(pathToRDF)
            fileWriter.write(result)
            fileWriter.close()
            updateInProgress = false
          }
        case Failure(exception) =>
          updateInProgress = false
          throw exception
      }
    }
    loadExistingModel(pathToRDF)
  }

}
