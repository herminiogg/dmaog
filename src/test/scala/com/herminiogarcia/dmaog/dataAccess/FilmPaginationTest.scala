package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator
import com.herminiogarcia.dmaog.dataAccess.generatedCode.FilmService
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@DoNotDiscover
class FilmPaginationTest extends AnyFunSuite with BeforeAndAfter {

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

  val filmService = new FilmService()
  val filmsServiceSparql = new generatedCodeSparql.FilmService()

  def removeOldData(): Unit = {
    filmsServiceSparql.getAll.forEach(filmsServiceSparql.delete(_))
  }

  def generateClasses(): Unit = {
    new File("./tmp").mkdir() //create temp directory for tests
    new CodeGenerator(Option(rules), "shexml", "./tmp/", "com.example",
      None, None, None, Some("http://localhost:3030/example"), Some("root"), Some("root"), None).generate()
  }

  test("Count is working as expected on local file") {
    assert(filmService.count() == 4)
  }

  test("Count is working as expected on SPARQL endpoint") {
    removeOldData()
    generateClasses()
    assert(filmsServiceSparql.count() == 4)
  }

  test("Pagination is returning the concerned elements correctly") {
    val items1 = filmService.getAll(2, 0)
    assert(items1.asScala.count(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }) == 1)
    assert(items1.asScala.count(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }) == 1)
    assert(items1.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }) == 0)
    assert(items1.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 0)

    val items2 =filmService.getAll(2, 2)
    assert(items2.asScala.count(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }) == 0)
    assert(items2.asScala.count(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }) == 0)
    assert(items2.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }) == 1)
    assert(items2.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 1)
  }

  test("Pagination is returning the concerned elements correctly from SPARQL endpoint") {
    removeOldData()
    generateClasses()
    val items1 = filmsServiceSparql.getAll(2, 0)
    assert(items1.asScala.count(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }) == 1)
    assert(items1.asScala.count(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }) == 1)
    assert(items1.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }) == 0)
    assert(items1.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 0)

    val items2 = filmsServiceSparql.getAll(2, 2)
    assert(items2.asScala.count(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }) == 0)
    assert(items2.asScala.count(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }) == 0)
    assert(items2.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }) == 1)
    assert(items2.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 1)
  }

}
