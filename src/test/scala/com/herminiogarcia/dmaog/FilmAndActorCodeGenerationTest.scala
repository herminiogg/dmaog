package com.herminiogarcia.dmaog

import org.scalatest.DoNotDiscover

@DoNotDiscover
class FilmAndActorCodeGenerationTest extends FilmCodeGenerationTest {

  override val rules =
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
      |    ITERATOR actors <cast/actor> {
      |        FIELD name <name>
      |        FIELD role <role>
      |        FIELD film <../../@id>
      |    }
      |    ITERATOR actresses <cast/actress> {
      |        FIELD name <name>
      |        FIELD role <role>
      |        FIELD film <../../@id>
      |    }
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
      |    ITERATOR actors <cast[*]> {
      |        FIELD name <name>
      |        FIELD role <role>
      |        POPPED_FIELD film <id>
      |    }
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
      |    schema:actor @:Actor ;
      |    schema:actor @:Actress ;
      |}
      |
      |:Actor dbr:[films.actors.name] {
      |    a :Actor ;
      |    :name [films.actors.name] ;
      |    :appear_on :[films.actors.film] ;
      |}
      |
      |:Actress dbr:[films.actresses.name] {
      |    a :Actor ;
      |    :name [films.actresses.name] ;
      |    :appear_on :[films.actresses.film] ;
      |}
      |""".stripMargin

  test("Actor class name and package are correctly generated") {
    val content = loadClass("Actor")
    assert(content.contains("public class Actor"))
    assert(content.contains("package com.example;"))
  }

  test("Film to Actor relation is correctly generated") {
    val content = loadClass("Film")

    assert(content.contains("private List<IRIValue> schemaActor;"))

    val setSchemaActor = "public Film setSchemaActor\\(List<IRIValue> schemaActor\\)[ \r\n]*[{][ \r\n]*this.schemaActor = schemaActor;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setSchemaActor.findFirstIn(content).isDefined)

    val getSchemaActor = "public List<IRIValue> getSchemaActor\\(\\)[ \r\n]*[{][ \r\n]*return this.schemaActor;[ \r\n]*[}]".r
    assert(getSchemaActor.findFirstIn(content).isDefined)
  }

  test("Actor attributes are correctly generated") {
    val content = loadClass("Actor")

    assert(content.contains("public final static String rdfType = \"http://example.com/Actor\";"))
    assert(content.contains("public final static String subjectPrefix = \"http://dbpedia.org/resource/\";"))

    assert(content.contains("private String name;"))
    assert(content.contains("private IRIValue appear_on;"))
    assert(content.contains("private IRIValue id;"))
  }

  test("Actor constructor is correctly generated") {
    val content = loadClass("Actor")

    assert("public Actor\\(\\)[ \r\n]*[{][ \r\n]*[}]".r.findFirstIn(content).isDefined)
  }

  test("Actor setters are correctly generated") {
    val content = loadClass("Actor")
    val setName = "public Actor setName\\(String name\\)[ \r\n]*[{][ \r\n]*this.name = name;[ \r\n]*return this;[ \r\n]*[}]".r
    val setAppear_on = "public Actor setAppear_on\\(IRIValue appear_on\\)[ \r\n]*[{][ \r\n]*this.appear_on = appear_on;[ \r\n]*return this;[ \r\n]*[}]".r
    val setId = "public Actor setId\\(IRIValue id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r

    assert(setName.findFirstIn(content).isDefined)
    assert(setAppear_on.findFirstIn(content).isDefined)
    assert(setId.findFirstIn(content).isDefined)
  }

  test("Actor getters are correctly generated") {
    val content = loadClass("Actor")
    val getName = "public String getName\\(\\)[ \r\n]*[{][ \r\n]*return this.name;[ \r\n]*[}]".r
    val getAppear_on = "public IRIValue getAppear_on\\(\\)[ \r\n]*[{][ \r\n]*return this.appear_on;[ \r\n]*[}]".r
    val getId = "public IRIValue getId\\(\\)[ \r\n]*[{][ \r\n]*return this.id;[ \r\n]*[}]".r

    assert(getName.findFirstIn(content).isDefined)
    assert(getAppear_on.findFirstIn(content).isDefined)
    assert(getId.findFirstIn(content).isDefined)
  }

}
