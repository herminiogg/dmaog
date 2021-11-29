package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.common.{IRIValue, ModelLoader, PrefixedNameConverter, ResourceLoader}
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.Model

import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable

class DataAccess(pathForGeneratedContent: String) extends ResourceLoader with ModelLoader with PrefixedNameConverter {

  def getAll[T](theClass: Class[T]): util.List[T] = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) =>
        val model = loadModel(pathForGeneratedContent + "/data.ttl")
        val sparql = loadFromResources("getAll.sparql").replaceFirst("\\$type", theType)
        val query = QueryFactory.create(sparql)
        val queryExecution = QueryExecutionFactory.create(query, model)
        val resultSet = queryExecution.execSelect()
        val groupedBySubjectResults = mutable.Map[String, List[(String, String)]]()
        while(resultSet.hasNext) {
          val result = resultSet.next()
          val subject = result.getResource("subject")
          val predicate = result.getResource("predicate")
          val theObject = result.get("object")
          val predicateURI = predicate.getURI
          val objectValue =
            if(theObject.isLiteral) theObject.asLiteral().getString
            else theObject.asResource().getURI
          groupedBySubjectResults.get(subject.getURI) match {
            case Some(value) =>
              val newList = value.+: (predicateURI, objectValue)
              groupedBySubjectResults.update(subject.getURI, newList)
            case None => groupedBySubjectResults.+= (subject.getURI -> List((predicateURI, objectValue)))
          }
        }
        populateObjects(groupedBySubjectResults.toMap, theClass, model).asJava
      case None => new util.ArrayList[T]()
    }
  }

  private def populateObjects[T](groupedBySubjectResults: Map[String, List[(String, String)]], theClass: Class[T], model: Model): List[T] = {
    val prefixes = model.getNsPrefixMap.asScala.toMap
    val prefixedNameConverterFunc = convertPrefixedName(prefixes)_
    val prefixedValueConverterFunc = convertPrefixedNameToValue(prefixes)_
    val results = for(key <- groupedBySubjectResults.keys) yield {
      val results = groupedBySubjectResults(key)
      val instance = theClass.newInstance()
      val methods = instance.getClass.getMethods
      methods.filter(_.getName == "setId").head.invoke(instance, createIRIValue(key, model))
      for(attribute <- results) {
        val methodName = "set" + prefixedNameConverterFunc(attribute._1).capitalize
        val value = prefixedValueConverterFunc(attribute._2)
        methods.find(_.getName == methodName).foreach(m => {
          val setterParameterType = m.getParameterTypes.head
          if(setterParameterType == classOf[IRIValue]) {
            m.invoke(instance, createIRIValue(attribute._2, model))
          } else {
            if(setterParameterType.getMethods.exists(_.getName == "valueOf") && setterParameterType != classOf[String]) {
              val numericConversion = setterParameterType.getMethod("valueOf", classOf[String])
              val parsedValue = numericConversion.invoke(setterParameterType, value)
              m.invoke(instance, parsedValue)
            } else {
              val castedValue = setterParameterType.cast(value)
              m.invoke(instance, castedValue.asInstanceOf[Object])
            }
          }
        })
      }
      instance
    }
    results.toList
  }

  private def getRDFType(theClass: Class[_]): Option[String] = {
    val instance = theClass.newInstance()
    val rdfType = instance.getClass.getField("rdfType")
    if(rdfType != null) {
      Some(rdfType.get(instance).toString)
    } else None
  }

  private def createIRIValue(iri: String, model: Model): IRIValue = {
    val prefixes = model.getNsPrefixMap.asScala.toMap
    val prefixedValueConverterFunc = convertPrefixedNameToValue(prefixes)_
    val localPart = prefixedValueConverterFunc(iri)
    new IRIValue(iri, iri.replaceFirst(localPart, ""), localPart)
  }

}
