package com.herminiogarcia.dmaog

import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatest.funsuite.AnyFunSuite

@DoNotDiscover
class FilmServiceTest extends AnyFunSuite with BeforeAndAfter with ClassGenerator {

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
      |    :year [films.year] xsd:integer ;
      |    schema:countryOfOrigin dbr:[films.country] ;
      |    schema:director dbr:[films.directors] ;
      |    :screenwritter dbr:[films.screenwritters] ;
      |    schema:musicBy dbr:[films.music] ;
      |    :cinematographer dbr:[films.photography] ;
      |}
      |""".stripMargin

  before {
    generateClasses()
  }

  test("Class name and package are correctly generated") {
    val content = loadClass("FilmService")
    assert(content.contains("public class FilmService"))
    assert(content.contains("package com.example;"))
  }

  test("Attributes are correctly generated") {
    val content = loadClass("FilmService")

    assert(content.contains("private DataAccess dataAccess;"))
  }

  test("Constructor is correctly generated") {
    val content = loadClass("FilmService")
    val constructor = "public FilmService[(][)][ \r\n]*[{][ \r\n]*this.dataAccess = DataAccessSingleton.getInstance[(][)];[ \r\n]*[}]".r

    assert(constructor.findFirstIn(content).isDefined)
  }

  test("Data access methods are correctly generated") {
    val content = loadClass("FilmService")
    val getAll = "public List<Film> getAll\\(\\)[ \r\n]*[{][ \r\n]*return dataAccess.getAll[(]Film.class[)];[ \r\n]*[}]".r
    val getAllRDF = "public String getAll\\(String rdfFormat\\)[ \r\n]*[{][ \r\n]*return dataAccess.getAll[(]Film.class, rdfFormat[)];[ \r\n]*[}]".r
    val getById = "public Optional<Film> getById[(]String id[)][ \r\n]*[{][ \r\n]*return dataAccess.getById[(]Film\\.class, id[)];[ \r\n]*[}]".r
    val getByIdRDF = "public String getById[(]String id, String rdfFormat[)][ \r\n]*[{][ \r\n]*return dataAccess.getById[(]Film\\.class, id, rdfFormat[)];[ \r\n]*[}]".r
    val getByField = "public List<Film> getByField[(]String fieldName, String value[)][ \r\n]*[{][ \r\n]*return dataAccess.getByField[(]Film\\.class, fieldName, value[)];[ \r\n]*[}]".r
    val getByFieldRDF = "public String getByField[(]String fieldName, String value, String rdfFormat[)][ \r\n]*[{][ \r\n]*return dataAccess.getByField[(]Film\\.class, fieldName, value, rdfFormat[)];[ \r\n]*[}]".r

    assert(getAll.findFirstIn(content).isDefined)
    assert(getById.findFirstIn(content).isDefined)
    assert(getByField.findFirstIn(content).isDefined)
    assert(getAllRDF.findFirstIn(content).isDefined)
    assert(getByIdRDF.findFirstIn(content).isDefined)
    assert(getByFieldRDF.findFirstIn(content).isDefined)
  }

}
