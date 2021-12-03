package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.common.{IRIValue, ModelLoader, PrefixedNameConverter, ResourceLoader}
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.Model

import java.lang.reflect.{Method, ParameterizedType}
import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable

class DataAccess(pathForGeneratedContent: String, mappingRules: String = null, mappingLanguage: String = "shexml", reloadMinutes: java.lang.Long = null) extends ResourceLoader with ModelLoader with PrefixedNameConverter {

  def getAll[T](theClass: Class[T]): util.List[T] = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) =>
        val model = loadModel(pathForGeneratedContent + "/data.ttl", Option(mappingRules), Option(mappingLanguage), Option(reloadMinutes))
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
        val attributeName = prefixedNameConverterFunc(attribute._1).capitalize
        val setterName = "set" + attributeName
        val getterName = "get" + attributeName
        val value = prefixedValueConverterFunc(attribute._2)
        methods.find(_.getName == setterName).foreach(m => {
          val getterMethod = methods.find(_.getName == getterName).headOption
          val isList = m.getParameterTypes.head == classOf[util.List[Object]]
          val setterParameterType =
            if(!isList) m.getParameterTypes.head
            else m.getGenericParameterTypes.head.asInstanceOf[ParameterizedType].getActualTypeArguments.headOption.map(_.asInstanceOf[Class[_]]).get
          if(setterParameterType == classOf[IRIValue]) {
            invokeSetterOrAddToList(m, getterMethod, instance, createIRIValue(attribute._2, model), isList)
          } else {
            if(setterParameterType.getMethods.exists(_.getName == "valueOf") && setterParameterType != classOf[String]) {
              val numericConversion = setterParameterType.getMethod("valueOf", classOf[String])
              val parsedValue = numericConversion.invoke(setterParameterType, value)
              invokeSetterOrAddToList(m, getterMethod, instance, parsedValue, isList)
            } else {
              val castedValue = setterParameterType.cast(value)
              invokeSetterOrAddToList(m, getterMethod, instance, castedValue.asInstanceOf[Object], isList)
            }
          }
        })
      }
      instance
    }
    results.toList
  }

  private def invokeSetterOrAddToList[T, S](setter: Method, getter: Option[Method], instance: T, value: S, list: Boolean): Unit = {
    if(list) {
      getter match {
        case Some(getterMethod) =>
          val possibleList = getterMethod.invoke(instance)
          val valueList = if(possibleList == null) {
            val newList = new util.ArrayList[S]()
            setter.invoke(instance, newList)
            newList
          } else possibleList
          valueList.asInstanceOf[util.List[S]].add(value)
        case None => throw new Exception("Getter equivalent for setter" + setter.getName + " not found")
      }
    } else {
      setter.invoke(instance, value.asInstanceOf[Object])
    }
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
