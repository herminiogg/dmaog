package com.herminiogarcia.dmaog

import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

class FilmCodeStaticGenerationTest extends AnyFunSuite with BeforeAndAfter with ClassGenerator {

  val rules =
    """
      |PREFIX : <http://example.com/>
      |PREFIX dbr: <http://dbpedia.org/resource/>
      |PREFIX schema: <http://schema.org/>
      |PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
      |SOURCE films_xml_file <http://shexml.herminiogarcia.com/files/films.xml>
      |SOURCE films_json_file <http://shexml.herminiogarcia.com/files/films.json>
      |ITERATOR film_xml <xpath: //film> {
      |    FIELD id <@id>
      |    FIELD name <name>
      |    FIELD year <year>
      |    FIELD country <country>
      |    FIELD directors <crew/directors/director>
      |    FIELD screenwritters <crew//screenwritter>
      |    FIELD music <crew/music>
      |    FIELD photography <crew/photography>
      |}
      |ITERATOR film_json <jsonpath: $.films[*]> {
      |    PUSHED_FIELD id <id>
      |    FIELD name <name>
      |    FIELD year <year>
      |    FIELD country <country>
      |    FIELD directors <crew.director>
      |    FIELD screenwritters <crew.screenwritter>
      |    FIELD music <crew.music>
      |    FIELD photography <crew.cinematography>
      |}
      |EXPRESSION films <films_xml_file.film_xml UNION films_json_file.film_json>
      |
      |:Films :[films.id] {
      |    a :Film ;
      |    schema:name [films.name] ;
      |    :year [films.year] xsd:integer ;
      |    schema:countryOfOrigin dbr:[films.country] ;
      |    schema:director dbr:[films.directors] ;
      |    :screenwritter dbr:[films.screenwritters] ;
      |    schema:musicBy dbr:[films.music] ;
      |    :cinematographer dbr:[films.photography] ;
      |}
      |""".stripMargin

  before {
    generateClasses(true)
  }

  test("Class name and package are correctly generated") {
    val content = loadClass("Film")
    assert(content.contains("public class Film"))
    assert(content.contains("package com.example;"))
  }

  test("Attributes are correctly generated") {
    val content = loadClass("Film")

    assert(content.contains("public final static String rdfType = \"http://example.com/Film\";"))
    assert(content.contains("public final static String subjectPrefix = \"http://example.com/\";"))

    assert(content.contains("private List<Integer> year;"))
    assert(content.contains("private List<String> schemaName;"))
    assert(content.contains("private List<IRIValue> screenwritter;"))
    assert(content.contains("private List<IRIValue> schemaCountryOfOrigin;"))
    assert(content.contains("private List<IRIValue> schemaDirector;"))
    assert(content.contains("private List<IRIValue> schemaMusicBy;"))
    assert(content.contains("private List<IRIValue> cinematographer;"))
    assert(content.contains("private IRIValue id;"))
  }

  test("Constructor is correctly generated") {
    val content = loadClass("Film")

    assert("public Film\\(\\)[ \r\n]*[{][ \r\n]*[}]".r.findFirstIn(content).isDefined)
  }

  test("Setters are correctly generated") {
    val content = loadClass("Film")
    val setYear = "public Film setYear\\(List<Integer> year\\)[ \r\n]*[{][ \r\n]*this.year = year;[ \r\n]*return this;[ \r\n]*[}]".r
    val setSchemaName = "public Film setSchemaName\\(List<String> schemaName\\)[ \r\n]*[{][ \r\n]*this.schemaName = schemaName;[ \r\n]*return this;[ \r\n]*[}]".r
    val setScreenwritter = "public Film setScreenwritter\\(List<IRIValue> screenwritter\\)[ \r\n]*[{][ \r\n]*this.screenwritter = screenwritter;[ \r\n]*return this;[ \r\n]*[}]".r
    val setSchemaCountryOfOrigin = "public Film setSchemaCountryOfOrigin\\(List<IRIValue> schemaCountryOfOrigin\\)[ \r\n]*[{][ \r\n]*this.schemaCountryOfOrigin = schemaCountryOfOrigin;[ \r\n]*return this;[ \r\n]*[}]".r
    val setSchemaDirector = "public Film setSchemaDirector\\(List<IRIValue> schemaDirector\\)[ \r\n]*[{][ \r\n]*this.schemaDirector = schemaDirector;[ \r\n]*return this;[ \r\n]*[}]".r
    val setSchemaMusicBy = "public Film setSchemaMusicBy\\(List<IRIValue> schemaMusicBy\\)[ \r\n]*[{][ \r\n]*this.schemaMusicBy = schemaMusicBy;[ \r\n]*return this;[ \r\n]*[}]".r
    val setCinematographer = "public Film setCinematographer\\(List<IRIValue> cinematographer\\)[ \r\n]*[{][ \r\n]*this.cinematographer = cinematographer;[ \r\n]*return this;[ \r\n]*[}]".r
    val setId = "public Film setId\\(IRIValue id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r

    assert(setYear.findFirstIn(content).isDefined)
    assert(setSchemaName.findFirstIn(content).isDefined)
    assert(setScreenwritter.findFirstIn(content).isDefined)
    assert(setSchemaCountryOfOrigin.findFirstIn(content).isDefined)
    assert(setSchemaDirector.findFirstIn(content).isDefined)
    assert(setSchemaMusicBy.findFirstIn(content).isDefined)
    assert(setCinematographer.findFirstIn(content).isDefined)
    assert(setId.findFirstIn(content).isDefined)
  }

  test("Getters are correctly generated") {
    val content = loadClass("Film")
    val getYear = "public List<Integer> getYear\\(\\)[ \r\n]*[{][ \r\n]*return this.year;[ \r\n]*[}]".r
    val getSchemaName = "public List<String> getSchemaName\\(\\)[ \r\n]*[{][ \r\n]*return this.schemaName;[ \r\n]*[}]".r
    val getScreenwritter = "public List<IRIValue> getScreenwritter\\(\\)[ \r\n]*[{][ \r\n]*return this.screenwritter;[ \r\n]*[}]".r
    val getSchemaCountryOfOrigin = "public List<IRIValue> getSchemaCountryOfOrigin\\(\\)[ \r\n]*[{][ \r\n]*return this.schemaCountryOfOrigin;[ \r\n]*[}]".r
    val getSchemaDirector = "public List<IRIValue> getSchemaDirector\\(\\)[ \r\n]*[{][ \r\n]*return this.schemaDirector;[ \r\n]*[}]".r
    val getSchemaMusicBy = "public List<IRIValue> getSchemaMusicBy\\(\\)[ \r\n]*[{][ \r\n]*return this.schemaMusicBy;[ \r\n]*[}]".r
    val getCinematographer = "public List<IRIValue> getCinematographer\\(\\)[ \r\n]*[{][ \r\n]*return this.cinematographer;[ \r\n]*[}]".r
    val getId = "public IRIValue getId\\(\\)[ \r\n]*[{][ \r\n]*return this.id;[ \r\n]*[}]".r

    assert(getYear.findFirstIn(content).isDefined)
    assert(getSchemaName.findFirstIn(content).isDefined)
    assert(getScreenwritter.findFirstIn(content).isDefined)
    assert(getSchemaCountryOfOrigin.findFirstIn(content).isDefined)
    assert(getSchemaDirector.findFirstIn(content).isDefined)
    assert(getSchemaMusicBy.findFirstIn(content).isDefined)
    assert(getCinematographer.findFirstIn(content).isDefined)
    assert(getId.findFirstIn(content).isDefined)
  }

}
