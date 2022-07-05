package com.herminiogarcia.dmaog.codeGeneration

import com.herminiogarcia.dmaog.common.DataTypedPredicate
import com.herminiogarcia.dmaog.common.Util.convertToJavaDataType
import com.herminiogarcia.shexml.MappingLauncher
import com.herminiogarcia.shexml.ast.{AST, Action, DataTypeLiteral, Declaration, DeclarationStatement, LiteralObject, LiteralObjectValue, LiteralSubject, ObjectElement, Prefix, ShExML, Shape, ShapeLink}
import org.apache.jena.datatypes.{RDFDatatype, TypeMapper}
import org.apache.jena.datatypes.xsd.XSDDatatype

import scala.util.{Failure, Success, Try}

object MappingRulesAnalyserFactory {
  def create(mappingRules: String, mappingLanguage: String): MappingRulesAnalyser = {
    if(mappingLanguage.toLowerCase == "shexml") ShExMLStaticRulesAnalyser(mappingRules)
    else if(mappingLanguage.toLowerCase == "rml") throw new Exception("RML not supported for static exploitation")
    else throw new Exception("The " + mappingLanguage + " mapping language is not supported")
  }
}

sealed trait MappingRulesAnalyser {
  def getTypes(): Map[Shape, String]
  def getAttributesPerType(): Map[Shape, List[DataTypedPredicate]]
  def getPrefixes(): Map[String, String]
  def getSubjectPrefix(theType: String): String
}

case class ShExMLStaticRulesAnalyser(mappingRules: String) extends MappingRulesAnalyser {

  def getTypes(): Map[Shape, String] = {
    (for(shape <- loadAST().shape) yield {
      val typeObject = shape.predicateObjects
        .find(po => po.predicate.prefix == "rdf:" && po.predicate.extension == "type")
      typeObject.map(to => {
        Try(to.objectOrShapeLink.asInstanceOf[LiteralObject])
      }).flatMap {
        case Success(value) => Some(value)
        case Failure(_) => Option.empty
      }.flatMap(lo => convertToURIType(lo)) match {
        case Some(value) => shape -> value
        case None =>
          val shapeParts = shape.shapeName.name.split(":")
          shape -> (getPrefixes()(shapeParts(0) + ":") + shapeParts(1))
      }
    }).toMap
  }

  def getAttributesPerType(): Map[Shape, List[DataTypedPredicate]] = {
    (for(shape <- loadAST().shape) yield {
      val attributes = shape.predicateObjects
        .filterNot(po => po.predicate.prefix == "rdf:" && po.predicate.extension == "type").map(po => {
          val predicate = getPrefixes().find(_._1 == po.predicate.prefix).get._2 + po.predicate.extension
          po.objectOrShapeLink match {
            case ObjectElement(prefix, _, _, _, dataType, _, _) => dataType.map {
              case DataTypeLiteral(value) =>
                val dataTypeURI = "http://www.w3.org/2001/XMLSchema#" + value.split(":")(1)
                val xsdType = TypeMapper.getInstance().getSafeTypeByName(dataTypeURI)
                //not having cardinality information, safest way is to assume every attribute can have multiple values
                val javaType = "List<" + convertToJavaDataType(xsdType) + ">"
                new DataTypedPredicate(predicate, javaType)
              case _=> generateTypeAccordingToPrefix(prefix, predicate)
            }.getOrElse(generateTypeAccordingToPrefix(prefix, predicate))
            case ShapeLink(_) => new DataTypedPredicate(predicate, "List<IRIValue>")
            case LiteralObject(_, _) => new DataTypedPredicate(predicate, "List<IRIValue>")
            case LiteralObjectValue(_) => new DataTypedPredicate(predicate, "List<String>")
          }
      })
      shape -> attributes
    }).toMap
  }

  private def generateTypeAccordingToPrefix(prefix: String, predicate: String): DataTypedPredicate = {
    if(prefix.isEmpty) new DataTypedPredicate(predicate, "List<String>")
    else new DataTypedPredicate(predicate, "List<IRIValue>")
  }

  def getPrefixes(): Map[String, String] = {
    loadAST().declaration.map(_.declarationStatement).filter(_.isInstanceOf[Prefix]).map {
      case Prefix(name, url) => (name.name -> url.url)
    }.toMap
  }

  def getSubjectPrefix(theType: String) = {
    getTypes().find(_._2 == theType).get._1.action match {
      case Action(shapePrefix, _) => getPrefixes().find(_._1 == shapePrefix).get._2
      case LiteralSubject(prefix, _) => getPrefixes().find(_._1 == prefix).get._2
    }
  }

  private def convertToURIType(literalObject: LiteralObject): Option[String] = {
    val prefixes = getPrefixes()
    prefixes.find(_._1 == literalObject.prefix.name).map(_._2 + literalObject.value)
  }

  private def loadAST(): ShExML = {
    val mappingLauncher = new MappingLauncher()
    val createLexerMethod = mappingLauncher.getClass.getDeclaredMethods.find(_.getName == "createLexer").get
    val createParserMethod = mappingLauncher.getClass.getDeclaredMethods.find(_.getName == "createParser").get
    val createASTMethod = mappingLauncher.getClass.getDeclaredMethods.find(_.getName == "createAST").get

    createLexerMethod.setAccessible(true)
    createParserMethod.setAccessible(true)
    createASTMethod.setAccessible(true)

    val lexer = createLexerMethod.invoke(mappingLauncher, mappingRules)
    val parser = createParserMethod.invoke(mappingLauncher, lexer)
    createASTMethod.invoke(mappingLauncher, parser).asInstanceOf[ShExML]
  }
}
