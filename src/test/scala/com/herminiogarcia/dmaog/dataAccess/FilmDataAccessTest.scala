package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.ClassGenerator
import com.herminiogarcia.dmaog.dataAccess.generatedCode.FilmService
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class FilmDataAccessTest extends AnyFunSuite with RDFStatementCreator {

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
