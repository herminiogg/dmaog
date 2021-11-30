package com.herminiogarcia.dmaog.codeGeneration

import com.herminiogarcia.dmaog.common.{DataTypedPredicate, ModelLoader, PrefixedNameConverter, ResourceLoader}
import es.weso.shexml.MappingLauncher
import org.apache.jena.datatypes.RDFDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.{Model, Resource}

import java.io.{File, PrintWriter}
import scala.collection.JavaConverters._
import scala.collection.mutable

class CodeGenerator(shexml: String, pathToGenerate: String, packageName: String) extends ResourceLoader
        with ModelLoader with PrefixedNameConverter {

  def generate(): Unit = {
    val pathToRDF = generateData()
    val model = loadModel(pathToRDF, None, None)
    val types = getTypes(model)
    val attributesPerType = getAttributesPerType(types, model)
    generateClasses(attributesPerType)
  }


  private def generateData(): String = {
    val rdfResult = new MappingLauncher().launchMapping(shexml, "Turtle")
    val finalPath = pathToGenerate + "/" + "data.ttl"
    writeFile(finalPath, rdfResult)
    finalPath
  }

  private def getTypes(model: Model): List[String] = {
    val query = QueryFactory.create(loadFromResources("getTypes.sparql"))
    val queryExecution = QueryExecutionFactory.create(query, model)
    val resultSet = queryExecution.execSelect()
    val types = mutable.ListBuffer[String]()
    while(resultSet.hasNext) {
      val results = resultSet.next()
      types.append(results.get("type").asResource().getURI)
    }
    types.toList
  }

  private def getAttributesPerType(types: List[String], model: Model): Map[String, List[DataTypedPredicate]] = {
    types.map(t => {
      val sparql = loadFromResources("getPredicatesByType.sparql").replaceFirst("\\$type", t)
      val query = QueryFactory.create(sparql)
      val queryExecution = QueryExecutionFactory.create(query, model)
      val resultSet = queryExecution.execSelect()
      val attributes = mutable.ListBuffer[DataTypedPredicate]()
      while(resultSet.hasNext) {
        val result = resultSet.next()
        val predicate = result.get("predicate").asResource().getURI
        val dataTypeSparql = loadFromResources("getDataTypeForPredicate.sparql")
          .replaceFirst("\\$type", t)
          .replaceFirst("\\$predicate", predicate)
        val dataTypeQuery = QueryFactory.create(dataTypeSparql)
        val dataTypeQueryExecution = QueryExecutionFactory.create(dataTypeQuery, model)
        val dataTypeResultSet = dataTypeQueryExecution.execSelect()
        val theObject = dataTypeResultSet.next().get("object")
        val dataType = if(theObject.isLiteral)
          convertToJavaDataType(theObject.asLiteral().getDatatype) // TODO: loop the results to find the best type
          else "IRIValue"
        attributes.append(new DataTypedPredicate(predicate, dataType))
      }
      t -> attributes.toList
    }).toMap
  }

  private def resourceToCapitalizedName(resource: Resource): String = {
    val localName = resource.getLocalName.capitalize
    val prefixName = resource.getNameSpace.capitalize
    prefixName + localName
  }

  private def generateClasses(attributesByType: Map[String, List[DataTypedPredicate]]): Unit = {
    val rdfsType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
    val model = loadModel(pathToGenerate + "/data.ttl", None, None)
    val prefixes = model.getNsPrefixMap.asScala.toMap
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
        .replaceAll("\\$attributes", attributesDeclaration)
        .replaceAll("\\$getters", getters)
        .replaceAll("\\$setters", setters)
      writeFile(capitalizedClassName + ".java", classCode)
      // Service class
      val serviceTemplate = loadFromResources("javaServiceClassGeneric.java")
      val serviceCode = serviceTemplate.replaceAll("\\$package", packageName)
        .replaceAll("\\$className", capitalizedClassName + "Service")
        .replaceAll("\\$type", capitalizedClassName)
      writeFile(capitalizedClassName + "Service.java", serviceCode)
    })
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

  private def writeFile(filename: String, content: String): Unit = {
    val file = new PrintWriter(new File(filename))
    file.write(content)
    file.close()
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



}
