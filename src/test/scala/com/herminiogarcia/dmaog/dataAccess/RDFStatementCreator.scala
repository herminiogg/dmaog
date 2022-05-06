package com.herminiogarcia.dmaog.dataAccess

import org.apache.jena.datatypes.RDFDatatype
import org.apache.jena.rdf.model.{Model, ModelFactory, Resource, ResourceFactory, Statement}
import org.apache.jena.riot.{RDFDataMgr, RDFLanguages}

import java.io.ByteArrayInputStream

trait RDFStatementCreator {

  def createStatement(subjectPrefix: String, s: String, predicatePrefix: String, p: String, objectPrefix: String, o: String): Statement = {
    val subject = ResourceFactory.createResource(subjectPrefix + s)
    val predicate = ResourceFactory.createProperty(predicatePrefix + p)
    val obj = ResourceFactory.createResource(objectPrefix + o)
    ResourceFactory.createStatement(subject, predicate, obj)
  }

  def createStatementWithResource(subjectPrefix: String, s: String, predicatePrefix: String, p: String, o: Resource): Statement = {
    val subject = ResourceFactory.createResource(subjectPrefix + s)
    val predicate = ResourceFactory.createProperty(predicatePrefix + p)
    ResourceFactory.createStatement(subject, predicate, o)
  }

  def createStatementWithLiteral(subjectPrefix: String, s: String, predicatePrefix: String, p: String, o: String, xsdType: RDFDatatype): Statement = {
    val subject = ResourceFactory.createResource(subjectPrefix + s)
    val predicate = ResourceFactory.createProperty(predicatePrefix + p)
    val obj = ResourceFactory.createTypedLiteral(o, xsdType)
    ResourceFactory.createStatement(subject, predicate, obj)
  }

  def loadModel(rdf: String): Model = {
    val model = ModelFactory.createDefaultModel()
    RDFDataMgr.read(model, new ByteArrayInputStream(rdf.getBytes), RDFLanguages.TURTLE)
    model
  }

}
