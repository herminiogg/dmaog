package com.herminiogarcia.dmaog.codeGeneration

import com.herminiogarcia.dmaog.common.Util.{convertToJavaDataType, getFinalPathToGenerate}
import com.herminiogarcia.dmaog.common.{DataTypedPredicate, MappingRulesRunner, ModelLoader, PrefixedNameConverter, PrefixesResolver, QueryExecutor, QueryExecutorFactory, ResourceLoader, SPARQLAuthentication, Util, WriterFactory}
import com.herminiogarcia.shexml.MappingLauncher
import com.typesafe.scalalogging.Logger
import org.apache.http.auth.AUTH
import org.apache.jena.datatypes.RDFDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory, ResultSet, ResultSetFactory}
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
                    sparqlEndpoint: Option[String], sparqlEndpointUsername: Option[String],
                    sparqlEndpointPassword: Option[String], sparqlQueryLimit: Option[String], datafile: Option[String],
                    staticExploitation: Boolean = false) extends ResourceLoader
        with ModelLoader with PrefixedNameConverter with MappingRulesRunner with SPARQLAuthentication {

  val namespaces: mutable.Map[String, String] = mutable.HashMap[String, String]()
  private val logger = Logger[CodeGenerator]
  
  initAuthenticationContext(sparqlEndpoint, sparqlEndpointUsername, sparqlEndpointPassword)

  def generate(): Unit = {
    if(staticExploitation && mappingRules.isDefined) {
      logger.info("Generating code statically from mapping rules")
      val mappingRulesAnalyser = createMappingRulesAnalyser
      val types = mappingRulesAnalyser.getTypes()
      val attributesPerType = mappingRulesAnalyser.getAttributesPerType()
      val finalAttributesPerType = types.map {
        case (k, v) => v -> attributesPerType(k)
      }
      generateClasses(finalAttributesPerType, getFinalPathToGenerate(pathToGenerate))
    } else {
      logger.info("Generating code dynamically from the existing data")
      val pathToRDF = generateData()
      def modelLoader = () => loadModel(pathToRDF, None, null, None, username, password, drivers, sparqlEndpoint)
      val types = getTypes(modelLoader, sparqlEndpoint)
      val attributesPerType = getAttributesPerType(types, modelLoader, sparqlEndpoint)
      generateClasses(attributesPerType, pathToRDF)
    }
  }


  private def generateData(): String = mappingRules match {
    case Some(rules) =>
      logger.info("Generating data from the mapping rules")
      val rdfResult = generateDataByMappingLanguage(rules, mappingLanguage, username, password, drivers)
      val result = Await.result(rdfResult, Duration.Inf)
      val temporalModel = ModelFactory.createDefaultModel()
      temporalModel.read(new ByteArrayInputStream(result.getBytes), null, "TTL")
      temporalModel.getNsPrefixMap.forEach { case (k, v) => namespaces += (k -> v) }
      WriterFactory.getWriter(pathToGenerate, sparqlEndpoint).write(result)
    case None => sparqlEndpoint.getOrElse(datafile.getOrElse(getFinalPathToGenerate(pathToGenerate)))
  }



  private def getTypes(model: () => Model, sparqlEndpoint: Option[String]): List[String] = {
    logger.info("Getting types")
    val resultSet = doSparqlQuery(loadFromResources("getTypes.sparql"), model, sparqlEndpoint)
    val types = mutable.ListBuffer[String]()
    while(resultSet.hasNext) {
      val results = resultSet.next()
      types.append(results.get("type").asResource().getURI)
    }
    types.toList
  }

  private def getSubjectByType(theType: String, finalPath: String): List[String] = {
    if(staticExploitation) {
      createMappingRulesAnalyser.getSubjectPrefix(theType) //TODO: change to analyse all the possible ocurrences too
    } else {
      def model = () => loadModel(finalPath, None, None, None, username, password, drivers, sparqlEndpoint)
      val resultSet = doSparqlQuery(loadFromResources("getSubjectsByType.sparql").replaceFirst("\\$type", theType), model, sparqlEndpoint)
      resultSet.asScala.toList.map(_.get("subject")).map(subjectResult => {
        if(subjectResult.isAnon) ""
        else {
          val uri = subjectResult.asResource().getURI
          Option(getPrefixes(finalPath).filter(p => uri.startsWith(p._2)).maxBy(_._2)).map(_._2).getOrElse({
            val slash = uri.lastIndexOf("/")
            val hash = uri.lastIndexOf("#")
            val maxIndex = slash.max(hash)
            uri.splitAt(maxIndex)._1 + (if(slash > hash) "/" else "#")
          })
        }
      }).distinct
    }

  }

  private def getAttributesPerType(types: List[String], model: () => Model, sparqlEndpoint: Option[String]): Map[String, List[DataTypedPredicate]] = {
    types.map(t => {
      val sparql = loadFromResources("getPredicatesByType.sparql").replaceFirst("\\$type", t)
      val resultSet = doSparqlQuery(sparql, model, sparqlEndpoint)
      val attributes = mutable.ListBuffer[DataTypedPredicate]()
      while(resultSet.hasNext) {
        // Predicate
        val result = resultSet.next()
        val predicate = result.get("predicate").asResource().getURI

        // Datatype
        val dataTypeSparql = loadFromResources("getDataTypeForPredicate.sparql")
          .replaceFirst("\\$type", t)
          .replaceFirst("\\$predicate", predicate)
        val dataTypeResultSet = doSparqlQuery(dataTypeSparql, model, sparqlEndpoint)
        val theObject = dataTypeResultSet.next().get("object")

        // Cardinality
        val cardinalitySparql = loadFromResources("getCardinalityForPredicate.sparql")
          .replaceFirst("\\$type", t)
          .replaceFirst("\\$predicate", predicate)
        val cardinalityResultSet = doSparqlQuery(cardinalitySparql, model, sparqlEndpoint)
        val objectCardinality = Try(cardinalityResultSet.next().get("cardinality").asLiteral().getInt) match {
          case Success(value) => value
          case Failure(exception) => 0
        }

        // Datatype with cardinality conversion
        val dataType =
          if(theObject.isLiteral &&
            (theObject.asLiteral().getLanguage.nonEmpty || theObject.asLiteral().getDatatypeURI == "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"))
            "MultilingualString"
          else if(theObject.isLiteral)
            convertToJavaDataType(theObject.asLiteral().getDatatype) // TODO: loop the results to find the best type
          else "IRIValue"
        val dataTypeWithCardinality = if(objectCardinality > 1) "List<" + dataType + ">" else dataType
        logger.debug(s"Detected attribute $predicate for type $t with data type and cardinality $dataTypeWithCardinality")
        attributes.append(new DataTypedPredicate(predicate, dataTypeWithCardinality))
      }
      t -> attributes.toList
    }).toMap
  }

  private def doSparqlQuery(sparql: String, model: () => Model, sparqlEndpoint: Option[String]): ResultSet = {
    logger.info("Executing SPARQL query")
    val finalSparqlQuery = sparql + sparqlQueryLimit.map(" LIMIT " + _).getOrElse("")
    logger.debug(finalSparqlQuery)
    val queryExecution = QueryExecutorFactory.getQueryExecutor(finalSparqlQuery, sparqlEndpoint, model)
    val result = Try(queryExecution.execute())
    result match {
      case Failure(e) =>
        logger.error(e.toString)
        logger.info("Error while executing the SPARQL query. We are going to retry once more.")
        Thread.sleep(60000) //wait for a minute and try the query again
        queryExecution.execute()
      case Success(s) => s
    }
  }

  private def resourceToCapitalizedName(resource: Resource): String = {
    val localName = resource.getLocalName.capitalize
    val prefixName = resource.getNameSpace.capitalize
    prefixName + localName
  }

  private def generateClasses(attributesByType: Map[String, List[DataTypedPredicate]], finalPath: String): Unit = {
    logger.info(s"Generating classes in $finalPath")
    val rdfsType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
    val prefixes = getPrefixes(finalPath)
    logger.debug(s"Loaded prefixes $prefixes")
    val convertPrefixedNameFunction = convertPrefixedName(prefixes)_
    attributesByType.keys.foreach(t => {
      logger.info(s"Generating classes for type: $t")
      val subjectsByType = getSubjectByType(t, finalPath)
      if(subjectsByType.size > 1) logger.info(s"Type $t uses more than one prefix. Generating different classes for each prefix.")
      subjectsByType.foreach(st => {
        val multiplePrefixesSuffix = if(subjectsByType.size > 1) subjectsByType.indexOf(st) + 1 else ""
        val capitalizedClassName = convertPrefixedNameFunction(t).capitalize + multiplePrefixesSuffix
        // DTO class
        val classTemplate = loadFromResources("javaClassGeneric.java")
        val attributes = attributesByType(t).filter(!_.predicate.equals(rdfsType)).:+(new DataTypedPredicate("id", "IRIValue"))
        val attributesDeclaration = attributes.map(a => generateAttributesCode("attribute.java", a.predicate, a.dataType, convertPrefixedNameFunction)).mkString("\n    ")
        val getters = attributes.map(a => generateGetterSetterCode("getter.java", capitalizedClassName, a.predicate, a.dataType, convertPrefixedNameFunction)).mkString("\n    ")
        val setters = attributes.map(a => generateGetterSetterCode("setter.java", capitalizedClassName, a.predicate, a.dataType, convertPrefixedNameFunction)).mkString("\n    ")
        val classCode = classTemplate.replaceAll("\\$package", packageName)
          .replaceAll("\\$className", capitalizedClassName)
          .replaceAll("\\$rdfType", t)
          .replaceAll("\\$subjectPrefix", st)
          .replaceAll("\\$attributes", attributesDeclaration)
          .replaceAll("\\$getters", getters)
          .replaceAll("\\$setters", setters)
        Util.writeFile(pathToGenerate, capitalizedClassName + ".java", classCode)
        logger.debug(s"Generated file ${capitalizedClassName + ".java"} with content: $classCode")
        // Service class
        val serviceTemplate = loadFromResources("javaServiceClassGeneric.java")
        val serviceCode = serviceTemplate.replaceAll("\\$package", packageName)
          .replaceAll("\\$className", capitalizedClassName + "Service")
          .replaceAll("\\$type", capitalizedClassName)
        Util.writeFile(pathToGenerate, capitalizedClassName + "Service.java", serviceCode)
        logger.debug(s"Generated file ${capitalizedClassName + "Service.java"} with content: $serviceCode")
      })
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
    logger.debug(s"Generated file DataAccessSingleton.java with content: $singletonCode")

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

  private def getPrefixes(finalPath: String): Map[String, String] = {
    if(staticExploitation) {
      logger.info("Prefixes lodaded from the mapping rules")
      val mappingRulesAnalyser = createMappingRulesAnalyser
      mappingRulesAnalyser.getPrefixes().map {
        case (k, v) => k.replaceFirst(":", "") -> v
      }
    } else {
      logger.info("Prefixes loaded from the data")
      val model = loadModel(finalPath, None, None, None, username, password, drivers, sparqlEndpoint, sparqlQueryLimit)
      if (model.getNsPrefixMap.isEmpty) namespaces.toMap
      else model.getNsPrefixMap.asScala.toMap
    }
  }

  private def createMappingRulesAnalyser: MappingRulesAnalyser = {
    MappingRulesAnalyserFactory.create(mappingRules.get, mappingLanguage)
  }

}

