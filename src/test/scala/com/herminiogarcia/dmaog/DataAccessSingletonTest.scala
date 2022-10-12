package com.herminiogarcia.dmaog

import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatest.funsuite.AnyFunSuite

@DoNotDiscover
class DataAccessSingletonTest extends AnyFunSuite with BeforeAndAfter with ClassGenerator {

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
    generateClasses()
  }

  test("Class name and package are correctly generated") {
    val content = loadClass("DataAccessSingleton")
    assert(content.contains("public class DataAccessSingleton"))
    assert(content.contains("package com.example;"))
  }

  test("Attributes are correctly generated") {
    val content = loadClass("DataAccessSingleton")

    assert(content.contains("private static DataAccess dataAccess;"))
    assert(content.contains("private static String dataFile = \"./tmp/data.ttl\";"))
    assert(content.contains("private static String mappingRules;"))
    assert(content.contains("private static String mappingLanguage;"))
    assert(content.contains("private static Long reloadMinutes;"))
    assert(content.contains("private static String username;"))
    assert(content.contains("private static String password;"))
    assert(content.contains("private static String password;"))
    assert(content.contains("private static String drivers = \"\";"))
    assert(content.contains("private static String sparqlEndpoint = null;"))
    assert("private static Map[<]String, String[>] prefixes = new HashMap[<]String, String[>][(][)] [{][{]".r.findFirstIn(content).isDefined)
    assert("[ \r\n\t]*put[(]\"dbr\",\"http://dbpedia.org/resource/\"[)];".r.findFirstIn(content).isDefined)
    assert("[ \r\n\t]*put[(]\"xsd\",\"http://www.w3.org/2001/XMLSchema#\"[)];".r.findFirstIn(content).isDefined)
    assert("[ \r\n\t]*put[(]\"schema\",\"http://schema.org/\"[)];".r.findFirstIn(content).isDefined)
    assert("[ \r\n\t]*put[(]\"\",\"http://example.com/\"[)];".r.findFirstIn(content).isDefined)
    assert("[ \r\n\t]*[}][}];".r.findFirstIn(content).isDefined)
  }

  test("getInstance is correctly generated") {
    val content = loadClass("DataAccessSingleton")
    val getInstance = ("public static DataAccess getInstance[(][)][ \r\n]*[{][ \r\n]*if[(]dataAccess == null[)]" +
      "[ \r\n]*[{][ \r\n]*dataAccess = new DataAccess[(]dataFile, mappingRules, mappingLanguage, reloadMinutes," +
      "[ \r\n\t]*username, password, drivers, sparqlEndpoint, prefixes[)];" +
      "[ \r\n]*[}][ \r\n]*return dataAccess;[ \r\n]*[}]").r

    assert(getInstance.findFirstIn(content).isDefined)
  }

}
