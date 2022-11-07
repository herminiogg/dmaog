package com.herminiogarcia.dmaog.common

import org.apache.jena.datatypes.RDFDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype

import java.io.{File, PrintWriter}

object Util {

  def writeFile(pathToGenerate: String, filename: String, content: String): Unit = {
    val file = new PrintWriter(new File(pathToGenerate + "/" + filename))
    file.write(content)
    file.close()
  }

  def isSparqlEndpoint(path: String): Boolean = {
    path.contains("http://") || path.contains("https://")
  }

  def getFinalPathToGenerate(pathToGenerate: String): String = {
    if(pathToGenerate.endsWith("/")) pathToGenerate + "data.ttl" else pathToGenerate + "/" + "data.ttl"
  }

  def convertToJavaDataType(datatype: RDFDatatype): String = datatype match {
    case XSDDatatype.XSDinteger => "Integer"
    case XSDDatatype.XSDint => "Integer"
    case XSDDatatype.XSDnegativeInteger => "Integer"
    case XSDDatatype.XSDnonNegativeInteger => "Integer"
    case XSDDatatype.XSDpositiveInteger => "Integer"
    case XSDDatatype.XSDnonPositiveInteger => "Integer"
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
    case XSDDatatype.XSDdate => "java.time.LocalDate"
    case XSDDatatype.XSDdateTime => "java.time.LocalDateTime"
    case XSDDatatype.XSDdateTimeStamp => "java.time.ZonedDateTime"
    case XSDDatatype.XSDtime => "java.time.LocalTime"
    case XSDDatatype.XSDgYear => "java.time.Year"
    case XSDDatatype.XSDgMonth => "String"
    case XSDDatatype.XSDgYearMonth => "java.time.YearMonth"
    case XSDDatatype.XSDgMonthDay => "java.time.MonthDay"
    case XSDDatatype.XSDgDay => "String"
    case XSDDatatype.XSDanyURI => "java.net.URI"
    case _ => throw new Exception("Impossible to convert the type " + datatype.getURI + " to a Java type")
  }
}
