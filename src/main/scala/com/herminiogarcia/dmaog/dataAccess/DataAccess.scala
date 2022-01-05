package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.common.{IRIValue, ModelLoader, PrefixedNameConverter, ResourceLoader}
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.Model

import java.lang.reflect.{Method, ParameterizedType}
import java.util
import java.util.Optional
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class DataAccess(fileNameForGeneratedContent: String,
                 mappingRules: String = null,
                 mappingLanguage: String = "shexml",
                 reloadMinutes: java.lang.Long = null,
                 username: String = null,
                 password: String = null,
                 drivers: String = "") extends ResourceLoader with ModelLoader with PrefixedNameConverter {

  private val nsPrefixes: Map[String, String] = getModel.getNsPrefixMap.asScala.toMap

  def getAll[T](theClass: Class[T]): util.List[T] = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) =>
        val model = getModel
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

  def getById[T](theClass: Class[T], id: String): Optional[T] = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) =>
        getSubjectPrefix(theClass) match {
          case Some(subjectPrefix) =>
            val model = getModel
            val fullSubjectIRI = subjectPrefix + id
            val sparql = loadFromResources("getById.sparql")
              .replaceFirst("\\$type", theType)
              .replaceFirst("\\$subjectIRI", fullSubjectIRI)
            val query = QueryFactory.create(sparql)
            val queryExecution = QueryExecutionFactory.create(query, model)
            val resultSet = queryExecution.execSelect()
            val groupedBySubjectResults = mutable.Map[String, List[(String, String)]]()
            while(resultSet.hasNext) {
              val result = resultSet.next()
              val predicate = result.getResource("predicate")
              val theObject = result.get("object")
              val predicateURI = predicate.getURI
              val objectValue =
                if (theObject.isLiteral) theObject.asLiteral().getString
                else theObject.asResource().getURI
              groupedBySubjectResults.get(fullSubjectIRI) match {
                case Some(value) =>
                  val newList = value.+:(predicateURI, objectValue)
                  groupedBySubjectResults.update(fullSubjectIRI, newList)
                case None => groupedBySubjectResults.+=(fullSubjectIRI -> List((predicateURI, objectValue)))
              }
            }
            populateObjects(groupedBySubjectResults.toMap, theClass, model).headOption match {
              case Some(value) => Optional.of(value)
              case None => Optional.empty()
            }
          case None => throw new Exception("The class " + theType + " has not a subjectPrefix field defined. Unable to construct the query without this information.")
        }
      case None => Optional.empty()
    }
  }

  def getByField[T](theClass: Class[T], fieldName: String, value: String): util.List[T] = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) =>
        getFullIRIForFieldName(theClass, fieldName) match {
          case Some(fullPredicateIRI) =>
            val model = getModel
            val sparql = loadFromResources("getSubjectsByField.sparql")
              .replaceFirst("\\$type", theType)
              .replaceFirst("\\$fieldIRI", fullPredicateIRI)
              .replaceFirst("\\$value", getValueForSparqlQuery(value))
            val query = QueryFactory.create(sparql)
            val queryExecution = QueryExecutionFactory.create(query, model)
            val resultSet = queryExecution.execSelect()
            val subjects = mutable.ListBuffer[String]()
            while(resultSet.hasNext) {
              val result = resultSet.next()
              val subjectURI = result.getResource("subject").getURI
              val namespace = model.getNsPrefixMap.asScala
                .find(p => subjectURI.startsWith(p._2)).head._2
              subjects.append(subjectURI.replaceFirst(namespace, ""))
            }
            subjects.toList.map(getById(theClass, _)).filter(_.isPresent).map(_.get()).asJava
          case None => throw new Exception("Field " + fieldName + " not found in type " + theClass)
        }
      case None => new util.ArrayList[T]()
    }
  }

  private def getValueForSparqlQuery(value: String): String = {
    if(value.startsWith("http://") || value.startsWith("https://"))
      "<" + value + ">"
    else {
      Try(value.toFloat) match {
        case Success(_) => value
        case Failure(_) => "'" + value + "'"
      }
    }
  }

  private def getModel = {
    loadModel(fileNameForGeneratedContent, Option(mappingRules), Option(mappingLanguage),
      Option(reloadMinutes), Option(username), Option(password), Option(drivers))
  }

  private def getFullIRIForFieldName[T](theClass: Class[T], fieldName: String) = {
    val prefixedNameConverterFunc = convertPrefixedName(nsPrefixes.toMap) _
    val prefix = nsPrefixes.keys.find(p => {
      if(p.isEmpty) false
      else fieldName.startsWith(p)
    })
    val fieldNameWithoutPrefix = prefix match {
      case Some(value) => fieldName.replaceFirst(value, "")
      case None =>
        if(nsPrefixes.keys.exists(_.isEmpty)) fieldName
        else throw new Exception("Field " + fieldName + " not found in the type " + theClass.getName)
    }
    val capitalizedFieldName = fieldNameWithoutPrefix.charAt(0).toLower + fieldNameWithoutPrefix.substring(1)
    nsPrefixes.toSeq.find(s => {
      prefixedNameConverterFunc(s._2 + capitalizedFieldName) == fieldName
    }).map(_._2 + capitalizedFieldName)
  }

  private def populateObjects[T](groupedBySubjectResults: Map[String, List[(String, String)]], theClass: Class[T], model: Model): List[T] = {
    val prefixedNameConverterFunc = convertPrefixedName(nsPrefixes)_
    val prefixedValueConverterFunc = convertPrefixedNameToValue(nsPrefixes)_
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
              val parsedValue = Try(numericConversion.invoke(setterParameterType, value)).getOrElse("null")
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
    if(value != null && String.valueOf(value) != "null") {
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
  }

  private def getRDFType(theClass: Class[_]): Option[String] = {
    val instance = theClass.newInstance()
    val rdfType = instance.getClass.getField("rdfType")
    if(rdfType != null) {
      Some(rdfType.get(instance).toString)
    } else None
  }

  private def getSubjectPrefix(theClass: Class[_]): Option[String] = {
    val instance = theClass.newInstance()
    val subjectPrefix = instance.getClass.getField("subjectPrefix")
    if(subjectPrefix != null) {
      Some(subjectPrefix.get(instance).toString)
    } else None
  }

  private def createIRIValue(iri: String, model: Model): IRIValue = {
    val prefixes = model.getNsPrefixMap.asScala.toMap
    val prefixedValueConverterFunc = convertPrefixedNameToValue(prefixes)_
    val localPart = prefixedValueConverterFunc(iri)
    val namespace = Try(iri.replaceFirst(localPart, "")) match {
      case Success(value) => value
      case Failure(_) => iri
    }
    new IRIValue(iri, namespace, localPart)
  }

}
