package com.herminiogarcia.dmaog.codeGeneration

import com.herminiogarcia.dmaog.common.{DataTypedPredicate, MappingRulesRunner, ModelLoader, PrefixedNameConverter, ResourceLoader, Util, WriterFactory}
import org.apache.jena.datatypes.RDFDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory, ResultSet}
import org.apache.jena.rdf.model.{Model, ModelFactory, Resource, Statement}
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.update.{UpdateExecutionFactory, UpdateFactory}

import java.io.ByteArrayInputStream
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class CodeGenerator(mappingRules: Option[String], mappingLanguage: String, pathToGenerate: String, packageName: String,
                   username: Option[String], password: Option[String], drivers: Option[String],
                    sparqlEndpoint: Option[String]) extends ResourceLoader
        with ModelLoader with PrefixedNameConverter with MappingRulesRunner {

  val namespaces: mutable.Map[String, String] = mutable.HashMap[String, String]()

  def generate(): Unit = {
    val pathToRDF = generateData()
    val model = loadModel(pathToRDF, None, null, None, username, password, drivers, sparqlEndpoint)
    val types = getTypes(model)
    val attributesPerType = getAttributesPerType(types, model)
    generateClasses(attributesPerType)
  }


  private def generateData(): String = mappingRules match {
    case Some(rules) =>
      val rdfResult = generateDataByMappingLanguage(rules, mappingLanguage, username, password, drivers)
      val result = Await.result(rdfResult, Duration.Inf)
      val temporalModel = ModelFactory.createDefaultModel()
      temporalModel.read(new ByteArrayInputStream(result.getBytes), null, "TTL")
      temporalModel.getNsPrefixMap.forEach { case (k, v) => namespaces += (k -> v) }
      WriterFactory.getWriter(pathToGenerate, sparqlEndpoint).write(result)
    case None => sparqlEndpoint.getOrElse(pathToGenerate + "/data.ttl")
  }

  private def getTypes(model: Model): List[String] = {
    val resultSet = doSparqlQuery(model, loadFromResources("getTypes.sparql"))
    val types = mutable.ListBuffer[String]()
    while(resultSet.hasNext) {
      val results = resultSet.next()
      types.append(results.get("type").asResource().getURI)
    }
    types.toList
  }

  private def getSubjectByType(theType: String, model: Model): String = {
    val resultSet = doSparqlQuery(model, loadFromResources("getSubjectsByType.sparql").replaceFirst("\\$type", theType))
    val result = resultSet.next()
    val uri = result.get("subject").asResource().getURI
    getPrefixes(model).find(p => uri.startsWith(p._2)).head._2
  }

  private def getAttributesPerType(types: List[String], model: Model): Map[String, List[DataTypedPredicate]] = {
    types.map(t => {
      val sparql = loadFromResources("getPredicatesByType.sparql").replaceFirst("\\$type", t)
      val resultSet = doSparqlQuery(model, sparql)
      val attributes = mutable.ListBuffer[DataTypedPredicate]()
      while(resultSet.hasNext) {
        // Predicate
        val result = resultSet.next()
        val predicate = result.get("predicate").asResource().getURI

        // Datatype
        val dataTypeSparql = loadFromResources("getDataTypeForPredicate.sparql")
          .replaceFirst("\\$type", t)
          .replaceFirst("\\$predicate", predicate)
        val dataTypeResultSet = doSparqlQuery(model, dataTypeSparql)
        val theObject = dataTypeResultSet.next().get("object")

        // Cardinality
        val cardinalitySparql = loadFromResources("getCardinalityForPredicate.sparql")
          .replaceFirst("\\$type", t)
          .replaceFirst("\\$predicate", predicate)
        val cardinalityResultSet = doSparqlQuery(model, cardinalitySparql)
        val objectCardinality = Try(cardinalityResultSet.next().get("cardinality").asLiteral().getInt) match {
          case Success(value) => value
          case Failure(exception) => 0
        }

        // Datatype with cardinality conversion
        val dataType = if(theObject.isLiteral)
          convertToJavaDataType(theObject.asLiteral().getDatatype) // TODO: loop the results to find the best type
          else "IRIValue"
        val dataTypeWithCardinality = if(objectCardinality > 1) "List<" + dataType + ">" else dataType
        attributes.append(new DataTypedPredicate(predicate, dataTypeWithCardinality))
      }
      t -> attributes.toList
    }).toMap
  }

  private def doSparqlQuery(model: Model, sparql: String): ResultSet = {
    val query = QueryFactory.create(sparql)
    val queryExecution = QueryExecutionFactory.create(query, model)
    val resultSet = queryExecution.execSelect()
    resultSet
  }

  private def resourceToCapitalizedName(resource: Resource): String = {
    val localName = resource.getLocalName.capitalize
    val prefixName = resource.getNameSpace.capitalize
    prefixName + localName
  }

  private def generateClasses(attributesByType: Map[String, List[DataTypedPredicate]]): Unit = {
    val rdfsType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
    val finalPath = if(pathToGenerate.endsWith("/")) pathToGenerate + "data.ttl" else pathToGenerate + "/" + "data.ttl"
    val model = loadModel(finalPath, None, None, None, username, password, drivers, sparqlEndpoint)
    val prefixes = getPrefixes(model)
    val convertPrefixedNameFunction = convertPrefixedName(prefixes)_
    attributesByType.keys.foreach(t => {
      val capitalizedClassName = convertPrefixedNameFunction(t).capitalize
      // DTO class
      val classTemplate = loadFromResources("javaClassGeneric.java")
      val attributes = attributesByType(t).filter(!_.predicate.equals(rdfsType)).:+(new DataTypedPredicate("id", "IRIValue"))
      val attributesDeclaration = attributes.map(a => generateAttributesCode("attribute.java", a.predicate, a.dataType, convertPrefixedNameFunction)).mkString("\n    ")
      val getters = attributes.map(a => generateGetterSetterCode("getter.java", capitalizedClassName, a.predicate, a.dataType, convertPrefixedNameFunction)).mkString("\n    ")
      val setters = attributes.map(a => generateGetterSetterCode("setter.java", capitalizedClassName, a.predicate, a.dataType, convertPrefixedNameFunction)).mkString("\n    ")
      val classCode = classTemplate.replaceAll("\\$package", packageName)
        .replaceAll("\\$className", capitalizedClassName)
        .replaceAll("\\$rdfType", t)
        .replaceAll("\\$subjectPrefix", getSubjectByType(t, model))
        .replaceAll("\\$attributes", attributesDeclaration)
        .replaceAll("\\$getters", getters)
        .replaceAll("\\$setters", setters)
      Util.writeFile(pathToGenerate, capitalizedClassName + ".java", classCode)
      // Service class
      val serviceTemplate = loadFromResources("javaServiceClassGeneric.java")
      val serviceCode = serviceTemplate.replaceAll("\\$package", packageName)
        .replaceAll("\\$className", capitalizedClassName + "Service")
        .replaceAll("\\$type", capitalizedClassName)
      Util.writeFile(pathToGenerate, capitalizedClassName + "Service.java", serviceCode)
    })
    val pathToDataFile = if(sparqlEndpoint.isEmpty) finalPath else ""
    val prefixesInMap = namespaces.map { case (k, v) => "put(\"" + k + "\",\"" + v + "\");" }.mkString("\n\t\t")
    val singletonCode = loadFromResources("javaDataAccessSingleton.java")
      .replaceFirst("\\$package", packageName)
      .replaceFirst("\\$drivers", drivers.getOrElse(""))
      .replaceFirst("\\$pathToData", pathToDataFile)
      .replaceFirst("\\$sparqlEndpoint", sparqlEndpoint.map('"' + _ + '"').getOrElse("null"))
      .replaceFirst("\\$prefixes", prefixesInMap)
    Util.writeFile(pathToGenerate, "DataAccessSingleton.java", singletonCode)

  }

  private def generateAttributesCode(template: String, attributeName: String, dataType: String, prefixedNameConverterFunc: String => String): String = {
    loadFromResources(template).replaceAll("\\$attributeName", prefixedNameConverterFunc(attributeName))
      .replaceFirst("\\$type", dataType)
  }

  private def generateGetterSetterCode(template: String, theType: String, attributeName: String, dataType: String, prefixedNameConverterFunc: String => String): String = {
    loadFromResources(template).replaceFirst("\\$attributeName", prefixedNameConverterFunc(attributeName).capitalize)
      .replaceAll("\\$attributeName", prefixedNameConverterFunc(attributeName))
      .replaceFirst("\\$type", dataType)
      .replaceFirst("\\$className", theType)  // only for the setters
  }

  private def convertToJavaDataType(datatype: RDFDatatype): String = datatype match {
    case XSDDatatype.XSDinteger => "Integer"
    case XSDDatatype.XSDint => "Integer"
    case XSDDatatype.XSDnegativeInteger => "Integer"
    case XSDDatatype.XSDpositiveInteger => "Integer"
    case XSDDatatype.XSDunsignedInt => "Integer"
    case XSDDatatype.XSDshort => "Integer"
    case XSDDatatype.XSDunsignedShort => "Integer"
    case XSDDatatype.XSDlong => "Long"
    case XSDDatatype.XSDunsignedLong => "Long"
    case XSDDatatype.XSDdecimal => "Float"
    case XSDDatatype.XSDfloat => "Float"
    case XSDDatatype.XSDdouble => "Double"
    case XSDDatatype.XSDstring => "String"
    case XSDDatatype.XSDboolean => "Boolean"
    case _ => throw new Exception("Impossible to convert the type " + datatype.getURI + " to a Java type")
  }

  private def getPrefixes(model: Model): Map[String, String] = {
    if(model.getNsPrefixMap.isEmpty) namespaces.toMap
    else model.getNsPrefixMap.asScala.toMap
  }
}

