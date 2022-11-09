package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.common.DatesConverter.{convertDateToJavaDate, isDate}
import com.herminiogarcia.dmaog.common.{BNode, DataLocalFileWriter, DatesConverter, IRIValue, ModelLoader, MultilingualString, PrefixedNameConverter, QueryExecutor, QueryExecutorFactory, ResourceLoader, SPARQLAuthentication}
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.query.{Dataset, DatasetFactory, QueryExecutionFactory, QueryFactory, QuerySolution, ResultSet, ResultSetFactory}
import org.apache.jena.rdf.model.{Model, ModelFactory, ResourceFactory}
import org.apache.jena.riot.{RDFDataMgr, RDFLanguages}
import org.apache.jena.update.{Update, UpdateExecutionFactory, UpdateFactory, UpdateProcessor, UpdateRequest}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.lang.reflect.{Method, ParameterizedType}
import java.util
import java.util.Optional
import scala.collection.JavaConverters.*
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class DataAccess(fileNameForGeneratedContent: String,
                 mappingRules: String = null,
                 mappingLanguage: String = "shexml",
                 reloadMinutes: java.lang.Long = null,
                 username: String = null,
                 password: String = null,
                 drivers: String = "",
                 sparqlEndpoint: String = "",
                 sparqlEndpointUsername: String = "",
                 sparqlEndpointPassword: String = "",
                 prefixes: java.util.Map[String, String]) extends ResourceLoader with ModelLoader
                                with PrefixedNameConverter with SPARQLAuthentication {

  initAuthenticationContext(sparqlEndpoint, sparqlEndpointUsername, sparqlEndpointPassword)

  val sparqlEndpointOption = if(sparqlEndpoint == null || sparqlEndpoint.isEmpty) Option.empty else Option(sparqlEndpoint)

  private val nsPrefixes: Map[String, String] = {
    val model = getModel
    if(model.getNsPrefixMap.isEmpty) prefixes.asScala.toMap else model.getNsPrefixMap.asScala.toMap
  }

  def getAll[T](theClass: Class[T]): util.List[T] = {
    getRDFType(theClass).map(t => {
      val sparql = loadFromResources("getAll.sparql").replaceFirst("\\$type", t)
      def getSubject: QuerySolution => String = getSubjectWhenBNodePossible
      def getPredicate: QuerySolution => String = r => r.getResource("predicate").getURI
      val groupedBySubjectResults = getGroupedStatements(sparql, getSubject, getPredicate)
      populateObjects(groupedBySubjectResults, theClass).asJava
    }).getOrElse(new util.ArrayList[T]())
  }


  def getAll[T](theClass: Class[T], rdfFormat: String): String = {
    getRDFType(theClass).map(t => {
      val sparql = loadFromResources("getAll.sparql").replaceFirst("\\$type", t)
      def getSubject: QuerySolution => String = getSubjectWhenBNodePossible
      def getPredicate: QuerySolution => String = r => r.getResource("predicate").getURI
      val groupedBySubjectResults = getGroupedStatements(sparql, getSubject, getPredicate)
      generateRDF(groupedBySubjectResults, rdfFormat)
    }).getOrElse("")
  }

  def getAll[T](theClass: Class[T], limit: java.lang.Long, offset: java.lang.Long): util.List[T] = {
    getRDFType(theClass).map(t => {
      val sparql = loadFromResources("getAllSubjects.sparql")
        .replaceFirst("\\$type", t)
        .replaceFirst("\\$limit", limit.toString)
        .replaceFirst("\\$offset", offset.toString)
      def getSubject: QuerySolution => String = getSubjectWhenBNodePossible
      def getPredicate: QuerySolution => String = r => "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
      val groupedBySubjectResults = getGroupedStatements(sparql, getSubject, getPredicate)
      groupedBySubjectResults.keys
        .map(k => getById(theClass, convertPrefixedNameToValue(nsPrefixes)(k)))
        .filter(_.isPresent).map(_.get()).toList.asJava
    }).getOrElse(new util.ArrayList[T]())
  }

  def count[T](theClass: Class[T]): java.lang.Long = {
    getRDFType(theClass).map(t => {
      val sparql = loadFromResources("countAll.sparql")
        .replaceFirst("\\$type", t)
      val resultSet = QueryExecutorFactory.getQueryExecutor(sparql, sparqlEndpointOption, () => getModel).execute()
      new java.lang.Long(resultSet.next().getLiteral("count").getLong)
    }).getOrElse(new java.lang.Long(0))
  }

  private def getGroupedStatements(sparql: String,
                                   getSubjectFunction: QuerySolution => String,
                                   getPredicateFunction: QuerySolution => String): Map[String, List[(String, ObjectResult)]] = {
    val resultSet = QueryExecutorFactory.getQueryExecutor(sparql, sparqlEndpointOption, () => getModel).execute()
    val groupedBySubjectResults = mutable.Map[String, List[(String, ObjectResult)]]()
    while(resultSet.hasNext) {
      val result = resultSet.next()
      val subject = getSubjectFunction(result)
      val predicate = getPredicateFunction(result)
      val theObject = result.get("object")
      val predicateURI = predicate
      val objectValue = {
        if(theObject == null) LiteralResult("")
        else if(theObject.isLiteral &&
          (theObject.asLiteral().getLanguage.nonEmpty || theObject.asLiteral().getDatatypeURI == "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"))
          MultilingualStringResult(theObject.asLiteral().getString, theObject.asLiteral().getLanguage)
        else if(theObject.isLiteral) LiteralResult(theObject.asLiteral().getString)
        else if(theObject.isAnon) BNodeResult(theObject.asResource().getId.getBlankNodeId.getLabelString)
        else IRIResult(theObject.asResource().getURI)
      }
      groupedBySubjectResults.get(subject) match {
        case Some(value) =>
          val newList = value.+: (predicateURI, objectValue)
          groupedBySubjectResults.update(subject, newList)
        case None => groupedBySubjectResults.+= (subject -> List((predicateURI, objectValue)))
      }
    }
    groupedBySubjectResults.toMap
  }

  def getById[T](theClass: Class[T], id: String): Optional[T] = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) => getSubjectPrefix(theClass) match {
        case Some(subjectPrefix) => {
          val fullSubjectIRI = subjectPrefix + id
          val sparql = loadFromResources("getById.sparql")
            .replaceFirst("\\$type", theType)
            .replaceFirst("\\$subjectIRI", fullSubjectIRI)
          def getSubject: QuerySolution => String = _ => fullSubjectIRI
          def getPredicate: QuerySolution => String = r => r.getResource("predicate").getURI
          val groupedBySubjectResults = getGroupedStatements(sparql, getSubject, getPredicate)
          populateObjects(groupedBySubjectResults, theClass).headOption match {
            case Some(value) => Optional.of(value)
            case None => Optional.empty()
          }
        }
        case None => throw new Exception ("The class " + theType + " has not a subjectPrefix field defined. Unable to construct the query without this information.")
      }
      case None => Optional.empty()
    }
  }

  def getById[T](theClass: Class[T], id: String, rdfFormat: String): String = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) => getSubjectPrefix(theClass) match {
        case Some(subjectPrefix) => {
          val fullSubjectIRI = subjectPrefix + id
          val sparql = loadFromResources("getById.sparql")
            .replaceFirst("\\$type", theType)
            .replaceFirst("\\$subjectIRI", fullSubjectIRI)
          def getSubject: QuerySolution => String = _ => fullSubjectIRI
          def getPredicate: QuerySolution => String = r => r.getResource("predicate").getURI
          val groupedBySubjectResults = getGroupedStatements(sparql, getSubject, getPredicate)
          generateRDF(groupedBySubjectResults, rdfFormat)
        }
        case None => throw new Exception ("The class " + theType + " has not a subjectPrefix field defined. Unable to construct the query without this information.")
      }
      case None => ""
    }
  }

  def getByField[T](theClass: Class[T], fieldName: String, value: String): util.List[T] = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) =>
        getFullIRIForFieldName(theClass, fieldName) match {
          case Some(fullPredicateIRI) =>
            doGetByField(theType, fullPredicateIRI, value).map(getById(theClass, _)).filter(_.isPresent).map(_.get()).asJava
          case None => throw new Exception("Field " + fieldName + " not found in type " + theClass)
        }
      case None => new util.ArrayList[T]()
    }
  }

  private def doGetByField[T](theType: String, fullPredicateIRI: String, value: String): List[String] = {
    val model = getModel
    val sparql = loadFromResources("getSubjectsByField.sparql")
      .replaceFirst("\\$type", theType)
      .replaceFirst("\\$fieldIRI", fullPredicateIRI)
      .replaceFirst("\\$value", getValueForSparqlQuery(value))
    val resultSet = QueryExecutorFactory.getQueryExecutor(sparql, sparqlEndpointOption, () => getModel).execute()
    val subjects = mutable.ListBuffer[String]()
    while(resultSet.hasNext) {
      val result = resultSet.next()
      val subjectURI = result.getResource("subject").getURI
      val namespace = nsPrefixes.find(p => subjectURI.startsWith(p._2)).head._2
      subjects.append(subjectURI.replaceFirst(namespace, ""))
    }
    subjects.toList
  }

  def getByField[T](theClass: Class[T], fieldName: String, value: String, rdfFormat: String): String = {
    val rdfType = getRDFType(theClass)
    rdfType match {
      case Some(theType) =>
        getFullIRIForFieldName(theClass, fieldName) match {
          case Some(fullPredicateIRI) =>
            val results = doGetByField(theType, fullPredicateIRI, value).map(getById(theClass, _, rdfFormat))
            val model = ModelFactory.createDefaultModel()
            model.setNsPrefixes(getModel.getNsPrefixMap)
            results.foreach(r => RDFDataMgr.read(model, new ByteArrayInputStream(r.getBytes), RDFLanguages.TURTLE))
            val outputStream = new ByteArrayOutputStream()
            val lang = RDFLanguages.nameToLang(rdfFormat)
            RDFDataMgr.write(outputStream, model, lang)
            outputStream.toString
          case None => throw new Exception("Field " + fieldName + " not found in type " + theClass)
        }
      case None => ""
    }
  }

  def insert[T](instance: T): Unit = {
    val triples = createTriplesForSparqlUpdate(instance, true)
    val sparql = loadFromResources("insertData.sparql")
      .replaceFirst("\\$prefixes", "")
      .replaceFirst("\\$triples", triples)
    updateExecution(sparql)
  }

  def delete[T](instance: T): Unit = {
    val triples = createTriplesForSparqlUpdate(instance, false)
    val sparql = loadFromResources("deleteData.sparql")
      .replaceFirst("\\$prefixes", "")
      .replaceAll("\\$triples", triples)
      .replaceFirst("\\$value", "<" + getIdValue(instance) + ">")
    updateExecution(sparql)
  }

  private def updateExecution(sparql: String): Unit = {
    val dataset = DatasetFactory.create(getModel)
    val updateQuery = UpdateFactory.create(sparql)
    createUpdateProcessor(updateQuery, dataset).execute()
    writeModelIfNotSparqlEndpoint(dataset.getDefaultModel)
  }

  private def createUpdateProcessor(updateQuery: UpdateRequest, dataset: Dataset): UpdateProcessor = {
    if(sparqlEndpoint == null || sparqlEndpoint.isEmpty) {
      UpdateExecutionFactory.create(updateQuery, dataset)
    } else UpdateExecutionFactory.createRemote(updateQuery, sparqlEndpoint)
  }

  private def writeModelIfNotSparqlEndpoint(model: Model): Unit = {
    if(sparqlEndpoint == null || sparqlEndpoint.isEmpty) {
      val outputStream = new ByteArrayOutputStream()
      model.write(outputStream, "Turtle")
      val parts = fileNameForGeneratedContent.split("/")
      val path = parts.slice(0, parts.length - 1).mkString("/")
      val filename = parts(parts.length - 1)
      val dataLocalFileWriter =
        if(filename == "data.ttl") DataLocalFileWriter(path)
        else DataLocalFileWriter(path, filename)
      dataLocalFileWriter.write(outputStream.toString)
      System.out.println("WARN: Updating on local disk is not meant for production environments and should be used only for testing purposes")
    }
  }

  private def createTriplesForSparqlUpdate[T](instance: T, isInsert: Boolean): String = {
    val methods = instance.getClass.getMethods.toList

    val typeURI = instance.getClass.getField("rdfType").get(instance).asInstanceOf[String]
    val subjectInstance = methods.find(_.getName == "getId").get.invoke(instance)
    val subjectURI =
      if(subjectInstance.isInstanceOf[BNode]) "_:" + subjectInstance.asInstanceOf[BNode].value
      else "<" + subjectInstance.asInstanceOf[IRIValue].iri + ">"

    val triples = methods.filterNot(m => m.getName.equals("getClass") || m.getName.equals("getId"))
        .filter(m => "get.*".r.findPrefixOf(m.getName).isDefined).flatMap(m => {
      val fieldName = m.getName.replaceFirst("get", "")
      val capitalizedFieldName = fieldName.head.toLower + fieldName.tail
      val fieldURI = getFullIRIForFieldName(instance.getClass, capitalizedFieldName)
      val value = m.invoke(instance)
      val values =
        if(value.isInstanceOf[java.util.List[AnyRef]]) value.asInstanceOf[java.util.List[AnyRef]].asScala
        else List(value)
      val values2 = values.map(v => {
        val objectTriple = {
          if(v.isInstanceOf[IRIValue]) Option("<" + v.asInstanceOf[IRIValue].iri + ">")
          else if(v.isInstanceOf[BNode]) Option("_:" + v.asInstanceOf[BNode].value)
          else if(v.isInstanceOf[MultilingualString])
            Option("\"" + v.asInstanceOf[MultilingualString].value + "\"" +"@" + v.asInstanceOf[MultilingualString].langTag)
          else if(v != null) Option("\"" + v.toString + "\"" +"^^<" + getXSDType(v).getURI + ">")
          else Option.empty
        }
        objectTriple.flatMap(ot => fieldURI.map(f => {
          val objectValueOrVariable = if(isInsert) ot else "?" + capitalizedFieldName
          val subjectURIOrVariable = if(isInsert) subjectURI else "?id"
          subjectURIOrVariable + " <" + f + "> " + objectValueOrVariable
        }))
      })
      values2.map(_.map(_ + " .\n"))

    }).filter(_.isDefined).map(_.get).mkString("")

    val subjectURIOrVariable = if(isInsert) subjectURI else "?id"
    subjectURIOrVariable + " a <" + typeURI + "> .\n" + triples
  }

  private def getIdValue[T](instance: T): String = {
    val method = instance.getClass.getMethods.toList.find(_.getName.equals("getId"))
    method.map(m => {
      val result = m.invoke(instance)
      if(result.isInstanceOf[BNode]) result.asInstanceOf[BNode].value
      else result.asInstanceOf[IRIValue].iri
    }) match {
      case Some(value) => value
      case None => throw new Exception("The class " + instance.getClass.getName + "does not have an id attribute. Try to regenerate the code!")
    }
  }

  private def getXSDType(value: AnyRef): XSDDatatype = {
    if(value.isInstanceOf[Integer]) XSDDatatype.XSDinteger
    else if(value.isInstanceOf[java.lang.Long]) XSDDatatype.XSDlong
    else if(value.isInstanceOf[java.lang.Float]) XSDDatatype.XSDfloat
    else if(value.isInstanceOf[java.lang.Double]) XSDDatatype.XSDdouble
    else if(value.isInstanceOf[java.lang.Boolean]) XSDDatatype.XSDboolean
    else if(value.isInstanceOf[java.time.LocalTime]) XSDDatatype.XSDtime
    else if(value.isInstanceOf[java.time.LocalDate]) XSDDatatype.XSDdate
    else if(value.isInstanceOf[java.time.LocalDateTime]) XSDDatatype.XSDdateTime
    else if(value.isInstanceOf[java.time.ZonedDateTime]) XSDDatatype.XSDdateTimeStamp
    else if(value.isInstanceOf[java.time.Year]) XSDDatatype.XSDgYear
    else if(value.isInstanceOf[java.time.Month]) XSDDatatype.XSDgMonth
    else if(value.isInstanceOf[java.time.YearMonth]) XSDDatatype.XSDgYearMonth
    else if(value.isInstanceOf[java.time.MonthDay]) XSDDatatype.XSDgMonthDay
    else if(value.isInstanceOf[java.time.DayOfWeek]) XSDDatatype.XSDgDay
    else if(value.isInstanceOf[java.net.URI]) XSDDatatype.XSDanyURI
    else if(value.isInstanceOf[String]) XSDDatatype.XSDstring
    else throw new Exception("Impossible to convert type " + value + " to an XSDDatatype")
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
      Option(reloadMinutes), Option(username), Option(password), Option(drivers), sparqlEndpointOption)
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

  private def populateObjects[T](groupedBySubjectResults: Map[String, List[(String, ObjectResult)]], theClass: Class[T]): List[T] = {
    val model = getModel
    val prefixedNameConverterFunc = convertPrefixedName(nsPrefixes)_
    val prefixedValueConverterFunc = convertPrefixedNameToValue(nsPrefixes)_
    val results = for(key <- groupedBySubjectResults.keys) yield {
      val results = groupedBySubjectResults(key)
      val instance = theClass.newInstance()
      val methods = instance.getClass.getMethods
      invokeSetId(methods.filter(_.getName == "setId").head, key, model, instance)
      for(attribute <- results) {
        val attributeName = prefixedNameConverterFunc(attribute._1).capitalize
        val setterName = "set" + attributeName
        val getterName = "get" + attributeName
        methods.find(_.getName == setterName).foreach(m => {
          val getterMethod = methods.find(_.getName == getterName).headOption
          val isList = m.getParameterTypes.head == classOf[util.List[Object]]
          val setterParameterType =
            if(!isList) m.getParameterTypes.head
            else m.getGenericParameterTypes.head.asInstanceOf[ParameterizedType].getActualTypeArguments.headOption.map(_.asInstanceOf[Class[_]]).get
          if(setterParameterType == classOf[IRIValue]) {
            invokeSetterOrAddToList(m, getterMethod, instance, createIRIValue(attribute._2.value, model), isList)
          } else if(setterParameterType == classOf[BNode]) {
            invokeSetterOrAddToList(m, getterMethod, instance, new BNode(attribute._2.value), isList)
          } else if(setterParameterType == classOf[MultilingualString]) {
            invokeSetterOrAddToList(m, getterMethod, instance,
              new MultilingualString(attribute._2.value, attribute._2.asInstanceOf[MultilingualStringResult].langtag), isList)
          } else {
            val value = prefixedValueConverterFunc(attribute._2.value)
            if(isDate(setterParameterType.getName)) {
              val convertedValue = convertDateToJavaDate(setterParameterType.getName, value)
              invokeSetterOrAddToList(m, getterMethod, instance, convertedValue, isList)
            }
            else if(setterParameterType.getMethods.exists(_.getName == "valueOf") && setterParameterType != classOf[String]) {
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

  private def invokeSetId[T](method: Method, value: String, model: Model, instance: T): Unit = {
    val typeName = method.getParameterTypes.head.getName
    val valueToSet =
      if(typeName == "com.herminiogarcia.dmaog.common.IRIValue") createIRIValue(value, model)
      else if(typeName == "com.herminiogarcia.dmaog.common.BNode") BNode(value)
      else throw new Exception(s"Type $typeName for subject does not match IRIValue nor BNode")
    method.invoke(instance, valueToSet)
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
    val prefixes = nsPrefixes
    val prefixedValueConverterFunc = convertPrefixedNameToValue(prefixes)_
    val localPart = prefixedValueConverterFunc(iri)
    val namespace = Try(iri.replaceFirst(localPart, "")) match {
      case Success(value) => value
      case Failure(_) => iri
    }
    new IRIValue(iri, namespace, localPart)
  }

  private def generateRDF[T](groupedBySubjectResults: Map[String, List[(String, ObjectResult)]], rdfFormat: String): String = {
    //new model and add prefixes
    val inputModel = getModel
    val modelToReturn = ModelFactory.createDefaultModel()
    modelToReturn.setNsPrefixes(inputModel.getNsPrefixMap)
    //generate new triple for each triple in the map
    groupedBySubjectResults.foreach({ case (subject, predicateObjects) => predicateObjects.foreach({
      case (predicate, theObject) =>
        inputModel.getResource(subject).listProperties(ResourceFactory.createProperty(predicate)).asScala.filter(s => {
          if(s.getObject.isLiteral && s.getObject.asLiteral().getLanguage.nonEmpty)
            s.getObject.asLiteral().getString == theObject.value &&
              s.getObject.asLiteral().getLanguage == theObject.asInstanceOf[MultilingualStringResult].langtag
          else if(s.getObject.isLiteral) s.getObject.asLiteral().getString == theObject.value
          else s.getObject.asResource().getURI == theObject.value
        }).foreach(modelToReturn.add)
    }) })
    //serialize in the appropriate format
    val outputStream = new ByteArrayOutputStream()
    val langValue = RDFLanguages.nameToLang(rdfFormat)
    RDFDataMgr.write(outputStream, modelToReturn, langValue)
    outputStream.toString
  }

  def getSubjectWhenBNodePossible(querySolution: QuerySolution): String = {
    val subject = querySolution.get("subject")
    if(subject.isAnon) subject.asResource().getId.getLabelString
    else subject.asResource().getURI
  }
}


sealed trait ObjectResult {
  val value: String
}

case class LiteralResult(value: String) extends ObjectResult
case class IRIResult(value: String) extends ObjectResult
case class BNodeResult(value: String) extends ObjectResult
case class MultilingualStringResult(value: String, langtag: String) extends ObjectResult
