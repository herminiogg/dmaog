package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.ClassGenerator
import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator
import com.herminiogarcia.dmaog.dataAccess.generatedCodeBNode.{ActorService, FilmService}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfter, DoNotDiscover}

import java.io.File
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@DoNotDiscover
class BNodeTest extends AnyFunSuite with BeforeAndAfter with ClassGenerator  {

  val rules =
    """
      |PREFIX : <http://example.com/>
      |PREFIX dbr: <http://dbpedia.org/resource/>
      |PREFIX schema: <http://schema.org/>
      |PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
      |SOURCE films_json_file <http://shexml.herminiogarcia.com/files/films.json>
      |ITERATOR film_json <jsonpath: $.films[*]> {
      |    PUSHED_FIELD id <id>
      |    FIELD name <name>
      |    FIELD year <year>
      |    ITERATOR actors <cast[*]> {
      |        FIELD name <name>
      |        POPPED_FIELD film <id>
      |    }
      |}
      |EXPRESSION films <films_json_file.film_json>
      |
      |:Films :[films.id] {
      |    a :Film ;
      |    schema:name [films.name] ;
      |    :year [films.year] xsd:integer ;
      |    schema:actor @:Actor ;
      |}
      |
      |:Actor _:[films.actors.name] {
      |    a :Actor ;
      |    :name [films.actors.name] ;
      |    :appear_on :[films.actors.film] ;
      |}
      |""".stripMargin

  val filmService = new FilmService()
  val actorService = new ActorService()

  def removeOldData(): Unit = {
    filmService.getAll.forEach(filmService.delete(_))
    actorService.getAll.forEach(actorService.delete(_))
  }

  def generateClasses(): Unit = {
    new File("./tmp").mkdir() //create temp directory for tests
    new CodeGenerator(Option(rules), "shexml", "./tmp/", "com.example",
      None, None, None, Some("http://localhost:3030/example"), Some("root"), Some("root"), None).generate()
  }

  before {
    removeOldData()
    generateClasses()
  }

  test("BNodes are correctly generated") {
    testCodeGeneration()
  }

  test("Film load data correctly") {
    testData()
  }

  test("BNodes are correctly generated on static generation") {
    generateClasses(true)

    testCodeGeneration()
  }

  def testCodeGeneration(): Unit = {
    val film = loadClass("Film")

    assert(film.contains("import com.herminiogarcia.dmaog.common.BNode;"))
    assert(film.contains("public final static String subjectPrefix = \"http://example.com/\";"))
    assert(film.contains("private IRIValue id;"))
    assert(film.contains("private List<BNode> schemaActor;"))

    val setId = "public Film setId\\(IRIValue id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setId.findFirstIn(film).isDefined)
    val getId = "public IRIValue getId\\(\\)[ \r\n]*[{][ \r\n]*return this.id;[ \r\n]*[}]".r
    assert(getId.findFirstIn(film).isDefined)

    val setActor = "public Film setSchemaActor\\(List<BNode> schemaActor\\)[ \r\n]*[{][ \r\n]*this.schemaActor = schemaActor;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setActor.findFirstIn(film).isDefined)
    val getActor = "public List<BNode> getSchemaActor\\(\\)[ \r\n]*[{][ \r\n]*return this.schemaActor;[ \r\n]*[}]".r
    assert(getActor.findFirstIn(film).isDefined)


    val actor = loadClass("Actor")

    assert(actor.contains("import com.herminiogarcia.dmaog.common.BNode;"))
    assert(actor.contains("public final static String subjectPrefix = \"\";"))
    assert(actor.contains("private BNode id;"))

    val setIdActor = "public Actor setId\\(BNode id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setIdActor.findFirstIn(actor).isDefined)
    val getIdActor = "public BNode getId\\(\\)[ \r\n]*[{][ \r\n]*return this.id;[ \r\n]*[}]".r
    assert(getIdActor.findFirstIn(actor).isDefined)
  }

  def testData(): Unit = {
    val filmService = new FilmService()
    val films = filmService.getAll

    assert(films.asScala.count(f => {
      f.getId.localName == "3" &&
        f.getYear == 2010 &&
        f.getSchemaName == "Inception" &&
        f.getSchemaActor.size() == 4
    }) == 1)
    assert(films.asScala.count(f => {
      f.getId.localName == "4" &&
        f.getYear == 2006 &&
        f.getSchemaName == "The Prestige" &&
        f.getSchemaActor.size() == 4
    }) == 1)

    val idsBNodesFilm3 = filmService.getById("3").get().getSchemaActor.asScala.map(_.value).toList
    val idsBNodesFilm4 = filmService.getById("4").get().getSchemaActor.asScala.map(_.value).toList

    val actorService = new ActorService()
    val actors = actorService.getAll

    assert(actors.asScala.find(a => {
        a.getName == "Tom Hardy" &&
        a.getAppear_on.iri == "http://example.com/3"
    }).isDefined)
    assert(actors.asScala.find(a => {
      a.getName == "Ellen Page" &&
        a.getAppear_on.iri == "http://example.com/3"
    }).isDefined)
    assert(actors.asScala.find(a => {
      a.getName == "Joseph Gordon-Levitt" &&
        a.getAppear_on.iri == "http://example.com/3"
    }).isDefined)
    assert(actors.asScala.find(a => {
      a.getName == "Leonardo DiCaprio" &&
        a.getAppear_on.iri == "http://example.com/3"
    }).isDefined)

    assert(actors.asScala.find(a => {
      a.getName == "Piper Perabo" &&
        a.getAppear_on.iri == "http://example.com/4"
    }).isDefined)
    assert(actors.asScala.find(a => {
      a.getName == "Michael Caine" &&
        a.getAppear_on.iri == "http://example.com/4"
    }).isDefined)
    assert(actors.asScala.find(a => {
      a.getName == "Christian Bale" &&
        a.getAppear_on.iri == "http://example.com/4"
    }).isDefined)
    assert(actors.asScala.find(a => {
      a.getName == "Hugh Jackman" &&
        a.getAppear_on.iri == "http://example.com/4"
    }).isDefined)
  }
}
