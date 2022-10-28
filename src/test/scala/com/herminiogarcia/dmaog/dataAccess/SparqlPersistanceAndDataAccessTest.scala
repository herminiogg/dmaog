package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator
import com.herminiogarcia.dmaog.dataAccess.generatedCodeSparql.FilmService
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import java.io.File

@DoNotDiscover
class SparqlPersistanceAndDataAccessTest extends AnyFunSuite with BeforeAndAfter {

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
      None, None, None, Some("http://localhost:3030/example"), Some("root"), Some("root"), None).generate()
  }

  val filmService = new FilmService()

  test("getAll is returning all members") {
    val items = filmService.getAll
    assert(items.asScala.count(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }) == 1)
    assert(items.asScala.count(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }) == 1)
    assert(items.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }) == 1)
    assert(items.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 1)
  }

  test("getById is returning only the requested member") {
    assert(filmService.getById("1").filter(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }).isPresent)
    assert(filmService.getById("2").filter(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }).isPresent)
    assert(filmService.getById("3").filter(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }).isPresent)
    assert(filmService.getById("4").filter(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }).isPresent)
  }

  test("getByField is returning the concerning members") {
    val items = filmService.getByField("schemaMusicBy", "http://dbpedia.org/resource/Hans_Zimmer")
    assert(items.asScala.count(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }) == 1)
    assert(items.asScala.count(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }) == 1)
    assert(items.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }) == 1)
    assert(items.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 0)
  }
}
