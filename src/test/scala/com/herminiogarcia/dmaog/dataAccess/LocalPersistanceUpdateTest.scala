package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator
import com.herminiogarcia.dmaog.dataAccess.generatedCodeUpdateLocalFile.FilmService
import org.scalatest.{BeforeAndAfter, DoNotDiscover}
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@DoNotDiscover
class LocalPersistanceUpdateTest extends AnyFunSuite with BeforeAndAfter {

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
    removeOldData()
    generateClasses()
  }

  def removeOldData(): Unit = {
    val filmService = new FilmService()
    filmService.getAll.forEach(filmService.delete(_))
  }

  def generateClasses(): Unit = {
    new File("./tmp").mkdir() //create temp directory for tests
    new CodeGenerator(Option(rules), "shexml", "./tmp/", "com.example",
      None, None, None, None, None, None, None, None).generate()
  }

  test("Updating a field works as expected") {
    val filmService = new FilmService()
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

    val film = filmService.getById("4").get()
    film.setYear(2011)
    filmService.commit(film)
    val updatedItems = filmService.getAll

    assert(updatedItems.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 0)

    assert(updatedItems.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2011 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 1)
  }

  test("Deleting an entity as expected") {
    val filmService = new FilmService()
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

    val film = filmService.getById("1").get()
    filmService.delete(film)
    val updatedItems = filmService.getAll

    assert(updatedItems.asScala.count(f => {
      f.getId.localName == "1" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2017 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Dunkirk"
    }) == 0)
    assert(updatedItems.asScala.count(f => {
      f.getId.localName == "2" &&
        f.getCinematographer.iri == "http://dbpedia.org/resource/Hoyte_van_Hoytema" &&
        f.getYear == 2014 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Interstellar"
    }) == 1)
    assert(updatedItems.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "Hans_Zimmer" &&
        f.getSchemaName == "Inception"
    }) == 1)
    assert(updatedItems.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaCountryOfOrigin.localName == "USA" &&
        f.getSchemaMusicBy.namespace == "http://dbpedia.org/resource/" &&
        f.getSchemaMusicBy.localName == "David_Julyan" &&
        f.getSchemaName == "The Prestige"
    }) == 1)
  }
}
