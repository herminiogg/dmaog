package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator
import com.herminiogarcia.dmaog.dataAccess.generatedCodeSparql.FilmService
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatest.funsuite.AnyFunSuite

import java.io.File

@DoNotDiscover
class FilmDataAccessRDFFromSPARQLEndpointTest extends AnyFunSuite with RDFStatementCreator with BeforeAndAfter {

  val filmService = new FilmService()
  val dbrPrefix = "http://dbpedia.org/resource/"
  val schemaPrefix = "http://schema.org/"
  val exPrefix = "http://example.com/"

  val rules =
    """
      |PREFIX : <http://example.com/>
      |PREFIX dbr: <http://dbpedia.org/resource/>
      |PREFIX schema: <http://schema.org/>
      |PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
      |SOURCE films_xml_file <https://shexml.herminiogarcia.com/files/films.xml>
      |SOURCE films_json_file <https://shexml.herminiogarcia.com/files/films.json>
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
      |    :nameWithLanguage [films.name] @en ;
      |    :year [films.year] xsd:integer ;
      |    schema:countryOfOrigin dbr:[films.country] ;
      |    schema:director dbr:[films.directors] ;
      |    :screenwritter dbr:[films.screenwritters] ;
      |    schema:musicBy dbr:[films.music] ;
      |    :cinematographer dbr:[films.photography] ;
      |}
      |""".stripMargin

  before {
    removeOldData()
    generateClasses()
  }

  after {
    removeOldData()
  }

  def removeOldData(): Unit = {
    filmService.getAll.forEach(filmService.delete(_))
  }

  def generateClasses(): Unit = {
    new File("./tmp").mkdir() //create temp directory for tests
    new CodeGenerator(Option(rules), "shexml", "./tmp/", "com.example",
      None, None, None, Some("http://localhost:3030/example"), Some("root"), Some("root"), None, None).generate()
  }

  test("getAll is returning in the RDF representation") {
    val items = loadModel(filmService.getAll("Turtle"))

    assert(items.contains(createStatement(exPrefix, "1", exPrefix, "cinematographer", dbrPrefix, "Hoyte_van_Hoytema")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "1", exPrefix, "year", "2017", XSDDatatype.XSDinteger)))
    assert(items.contains(createStatement(exPrefix, "1", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(items.contains(createStatement(exPrefix, "1", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "1", schemaPrefix, "name", "Dunkirk", XSDDatatype.XSDstring)))

    assert(items.contains(createStatement(exPrefix, "2", exPrefix, "cinematographer", dbrPrefix, "Hoyte_van_Hoytema")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "2", exPrefix, "year", "2014", XSDDatatype.XSDinteger)))
    assert(items.contains(createStatement(exPrefix, "2", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(items.contains(createStatement(exPrefix, "2", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "2", schemaPrefix, "name", "Interstellar", XSDDatatype.XSDstring)))

    assert(items.contains(createStatementWithLiteral(exPrefix, "3", exPrefix, "year", "2010", XSDDatatype.XSDinteger)))
    assert(items.contains(createStatement(exPrefix, "3", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(items.contains(createStatement(exPrefix, "3", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "3", schemaPrefix, "name", "Inception", XSDDatatype.XSDstring)))

    assert(items.contains(createStatementWithLiteral(exPrefix, "4", exPrefix, "year", "2006", XSDDatatype.XSDinteger)))
    assert(items.contains(createStatement(exPrefix, "4", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(items.contains(createStatement(exPrefix, "4", schemaPrefix, "musicBy", dbrPrefix, "David_Julyan")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "4", schemaPrefix, "name", "The Prestige", XSDDatatype.XSDstring)))
  }

  test("getById is returning only the requested member") {
    val film1 = loadModel(filmService.getById("1", "Turtle"))
    val film2 = loadModel(filmService.getById("2", "Turtle"))
    val film3 = loadModel(filmService.getById("3", "Turtle"))
    val film4 = loadModel(filmService.getById("4", "Turtle"))

    assert(film1.contains(createStatement(exPrefix, "1", exPrefix, "cinematographer", dbrPrefix, "Hoyte_van_Hoytema")))
    assert(film1.contains(createStatementWithLiteral(exPrefix, "1", exPrefix, "year", "2017", XSDDatatype.XSDinteger)))
    assert(film1.contains(createStatement(exPrefix, "1", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(film1.contains(createStatement(exPrefix, "1", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(film1.contains(createStatementWithLiteral(exPrefix, "1", schemaPrefix, "name", "Dunkirk", XSDDatatype.XSDstring)))

    assert(film2.contains(createStatement(exPrefix, "2", exPrefix, "cinematographer", dbrPrefix, "Hoyte_van_Hoytema")))
    assert(film2.contains(createStatementWithLiteral(exPrefix, "2", exPrefix, "year", "2014", XSDDatatype.XSDinteger)))
    assert(film2.contains(createStatement(exPrefix, "2", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(film2.contains(createStatement(exPrefix, "2", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(film2.contains(createStatementWithLiteral(exPrefix, "2", schemaPrefix, "name", "Interstellar", XSDDatatype.XSDstring)))

    assert(film3.contains(createStatementWithLiteral(exPrefix, "3", exPrefix, "year", "2010", XSDDatatype.XSDinteger)))
    assert(film3.contains(createStatement(exPrefix, "3", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(film3.contains(createStatement(exPrefix, "3", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(film3.contains(createStatementWithLiteral(exPrefix, "3", schemaPrefix, "name", "Inception", XSDDatatype.XSDstring)))

    assert(film4.contains(createStatementWithLiteral(exPrefix, "4", exPrefix, "year", "2006", XSDDatatype.XSDinteger)))
    assert(film4.contains(createStatement(exPrefix, "4", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(film4.contains(createStatement(exPrefix, "4", schemaPrefix, "musicBy", dbrPrefix, "David_Julyan")))
    assert(film4.contains(createStatementWithLiteral(exPrefix, "4", schemaPrefix, "name", "The Prestige", XSDDatatype.XSDstring)))
  }

  test("getByField is returning the concerning members") {
    val items = loadModel(filmService.getByField("schemaMusicBy", "http://dbpedia.org/resource/Hans_Zimmer", "Turtle"))

    assert(items.contains(createStatement(exPrefix, "1", exPrefix, "cinematographer", dbrPrefix, "Hoyte_van_Hoytema")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "1", exPrefix, "year", "2017", XSDDatatype.XSDinteger)))
    assert(items.contains(createStatement(exPrefix, "1", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(items.contains(createStatement(exPrefix, "1", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "1", schemaPrefix, "name", "Dunkirk", XSDDatatype.XSDstring)))

    assert(items.contains(createStatement(exPrefix, "2", exPrefix, "cinematographer", dbrPrefix, "Hoyte_van_Hoytema")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "2", exPrefix, "year", "2014", XSDDatatype.XSDinteger)))
    assert(items.contains(createStatement(exPrefix, "2", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(items.contains(createStatement(exPrefix, "2", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "2", schemaPrefix, "name", "Interstellar", XSDDatatype.XSDstring)))

    assert(items.contains(createStatementWithLiteral(exPrefix, "3", exPrefix, "year", "2010", XSDDatatype.XSDinteger)))
    assert(items.contains(createStatement(exPrefix, "3", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(items.contains(createStatement(exPrefix, "3", schemaPrefix, "musicBy", dbrPrefix, "Hans_Zimmer")))
    assert(items.contains(createStatementWithLiteral(exPrefix, "3", schemaPrefix, "name", "Inception", XSDDatatype.XSDstring)))

    assert(!items.contains(createStatementWithLiteral(exPrefix, "4", exPrefix, "year", "2006", XSDDatatype.XSDinteger)))
    assert(!items.contains(createStatement(exPrefix, "4", schemaPrefix, "countryOfOrigin", dbrPrefix, "USA")))
    assert(!items.contains(createStatement(exPrefix, "4", schemaPrefix, "musicBy", dbrPrefix, "David_Julyan")))
    assert(!items.contains(createStatementWithLiteral(exPrefix, "4", schemaPrefix, "name", "The Prestige", XSDDatatype.XSDstring)))
  }

  test("Test language tags are correctly generated") {
    val items = loadModel(filmService.getAll("Turtle"))

    assert(items.contains(createStatementWithMultilingualLiteral(exPrefix, "1", exPrefix, "nameWithLanguage", "Dunkirk", "en")))
    assert(items.contains(createStatementWithMultilingualLiteral(exPrefix, "2", exPrefix, "nameWithLanguage", "Interstellar", "en")))
    assert(items.contains(createStatementWithMultilingualLiteral(exPrefix, "3", exPrefix, "nameWithLanguage", "Inception", "en")))
    assert(items.contains(createStatementWithMultilingualLiteral(exPrefix, "4", exPrefix, "nameWithLanguage", "The Prestige", "en")))
  }
}
