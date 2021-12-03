package com.herminiogarcia.dmaog.common

import com.herminiogarcia.com.herminiogarcia.dmaog.common.MappingRulesRunner
import es.weso.shexml.MappingLauncher
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.RDFDataMgr

import java.io.{File, FileWriter}
import java.util.Date

trait ModelLoader extends MappingRulesRunner {

  protected def loadModel(pathToRDF: String, mappingRules: Option[String], mappingLanguage: Option[String], reloadMinutes: Option[Long]): Model = mappingRules match {
    case Some(rules) =>
      if(!new File(pathToRDF).exists()) applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"))
      else reloadMinutes match {
        case Some(minutes) =>
          val modifiedTime = new File(pathToRDF).lastModified()
          val currentTime = new Date().getTime
          val reloadMillis = minutes * 60 * 1000
          if((currentTime - modifiedTime) > reloadMillis) {
            applyMappingRules(pathToRDF, rules, mappingLanguage.getOrElse("shexml"))
          } else {
            loadExistingModel(pathToRDF)
          }
        case None => loadExistingModel(pathToRDF)
      }
    case None => loadExistingModel(pathToRDF)
  }

  private def loadExistingModel(pathToRDF: String) = {
    RDFDataMgr.loadModel(pathToRDF)
  }

  private def applyMappingRules(pathToRDF: String, mappingRules: String, mappingLanguage: String): Model = {
    val turtle = generateDataByMappingLanguage(mappingRules, mappingLanguage)
    val fileWriter = new FileWriter(pathToRDF)
    fileWriter.write(turtle)
    fileWriter.close()
    loadExistingModel(pathToRDF)
  }

}
