package com.herminiogarcia
package com.herminiogarcia.dmaog.common

import be.ugent.rml.{Executor, Utils}
import be.ugent.rml.functions.FunctionLoader
import be.ugent.rml.records.RecordsFactory
import be.ugent.rml.store.{QuadStoreFactory, RDF4JStore}
import be.ugent.rml.term.NamedNode
import es.weso.shexml.MappingLauncher
import org.apache.jena.query.DatasetFactory
import org.apache.jena.riot.{RDFDataMgr, RDFLanguages}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.reflect.io.File

trait MappingRulesRunner {

  def generateDataByMappingLanguage(mappingRules: String, mappingLanguage: String,
                                    username: Option[String], password: Option[String],
                                    drivers: Option[String]): String = {
    val language = mappingLanguage.toLowerCase()
    if(language == "shexml") {
      new MappingLauncher(username.getOrElse(""), password.getOrElse(""), drivers.getOrElse("")).launchMapping(mappingRules, "Turtle")
    } else if(language == "rml") {
      val mappingStream = new ByteArrayInputStream(mappingRules.getBytes)
      val rmlStore = QuadStoreFactory.read(mappingStream)
      val factory = new RecordsFactory(File(".").toAbsolute.path)
      val functionLoader = new FunctionLoader()
      val outputStore = new RDF4JStore()
      val executor = new Executor(rmlStore, factory, functionLoader, outputStore, Utils.getBaseDirectiveTurtle(mappingStream))
      val result = executor.execute(null)
      val stream = new ByteArrayOutputStream()
      val dataset = DatasetFactory.create()
      result.write(stream, "turtle")
      RDFDataMgr.read(dataset, new ByteArrayInputStream(stream.toByteArray), RDFLanguages.TURTLE)

      // Add prefixes
      val rmlDataset = DatasetFactory.create()
      RDFDataMgr.read(rmlDataset, new ByteArrayInputStream(mappingRules.getBytes), RDFLanguages.TURTLE)
      val prefixes = rmlDataset.getDefaultModel.getNsPrefixMap
      dataset.getDefaultModel.setNsPrefixes(prefixes)

      // Generate string
      val outputStream = new ByteArrayOutputStream()
      dataset.getDefaultModel.write(outputStream, "TURTLE")
      outputStream.toString
    } else throw new Exception("Mapping language " + mappingLanguage + " is not supported in this version.")
  }

}
