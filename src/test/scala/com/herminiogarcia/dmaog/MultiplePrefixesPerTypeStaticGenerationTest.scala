package com.herminiogarcia.dmaog

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfter, DoNotDiscover}

@DoNotDiscover
class MultiplePrefixesPerTypeStaticGenerationTest extends AnyFunSuite with BeforeAndAfter with ClassGenerator {

  override val rules =
    """
      |PREFIX wd: <http://www.wikidata.org/entity/>
      |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      |PREFIX : <http://example.com/>
      |PREFIX ehri: <https://lod.ehri-project-test.eu/>
      |PREFIX ehri_country: <https://lod.ehri-project-test.eu/countries/>
      |#TODO instutions with mixed paths
      |PREFIX ehri_institution: <https://lod.ehri-project-test.eu/institutions/>
      |PREFIX ehri_units: <https://lod.ehri-project-test.eu/units/>
      |PREFIX ehri_pers: <https://lod.ehri-project-test.eu/vocabularies/ehri-pers/>
      |PREFIX ehri_pers_full_name: <https://lod.ehri-project-test.eu/vocabularies/ehri-pers/name/>
      |PREFIX ehri_pers_other_form_name: <https://lod.ehri-project-test.eu/vocabularies/ehri-pers/other-name/>
      |PREFIX ehri_pers_parallel_form_name: <https://lod.ehri-project-test.eu/vocabularies/ehri-pers/parallel-name/>
      |PREFIX dbr: <http://dbpedia.org/resource/>
      |PREFIX schema: <http://schema.org/>
      |PREFIX xs: <http://www.w3.org/2001/XMLSchema#>
      |PREFIX owl: <http://www.w3.org/2002/07/owl#>
      |PREFIX rico: <https://www.ica.org/standards/RiC/ontology#>
      |SOURCE people <dummy.json>
      |ITERATOR people_iterator <jsonpath: $.data.AuthoritativeSet.authorities.items[*]> {
      |	PUSHED_FIELD term_id <identifier>
      |    FIELD other_form_term_id <[?(@.description.otherFormsOfName[0])].identifier>
      |    FIELD parallel_name_term_id <[?(@.description.parallelFormsOfName[0])].identifier>
      |    FIELD name <description.name>
      |    FIELD lastName <description.lastName>
      |    FIELD firstName <description.firstName>
      |    FIELD languageCode <description.languageCode>
      |    FIELD src <description.source>
      |    FIELD datesOfExistence <description.datesOfExistence>
      |    FIELD biographicalHistory <description.biographicalHistory>
      |    FIELD otherFormsOfName <description.otherFormsOfName>
      |    FIELD parallelFormsOfName <description.parallelFormsOfName>
      |  	ITERATOR links <links[*]> {
      |          FIELD fakefield <fakefield>
      |          ITERATOR targets <targets[?(@.type=='DocumentaryUnit')]> {
      |              FIELD unit_id <id>
      |              POPPED_FIELD term_id <term_id>
      |          }
      |      }
      |}
      |EXPRESSION person <people.people_iterator>
      |AUTOINCREMENT agent_name_id <"agentName" + 0 to 99999999>
      |ehri:Link ehri_units:[person.links.targets.unit_id] {
      |    rico:hasOrHadSubject ehri_pers:[person.links.targets.term_id] ;
      |}
      |ehri:Person ehri_pers:[person.term_id] {
      |    a rico:Person ;
      |    rdfs:label [person.name] @[person.languageCode] ;
      |    rico:history [person.biographicalHistory] ;
      |    rico:hasAgentName @ehri:AgentOtherFormName ;
      |    rico:hasAgentName @ehri:AgentParallelFormName ;
      |}
      |ehri:AgentOtherFormName ehri_pers_other_form_name:[person.other_form_term_id] {
      |	a rico:AgentName ;
      |	rdfs:label [person.otherFormsOfName] ;
      |}
      |ehri:AgentParallelFormName ehri_pers_parallel_form_name:[person.parallel_name_term_id] {
      |	a rico:AgentName ;
      |	rdfs:label [person.parallelFormsOfName] ;
      |}
      |""".stripMargin

  before {
    generateClasses(true)
  }

  test("Class 1 is correctly generated") {
    val content = loadClass("RicoAgentName1")
    assert(content.contains("public class RicoAgentName1"))
    assert(content.contains("package com.example;"))

    assert(content.contains("public final static String rdfType = \"https://www.ica.org/standards/RiC/ontology#AgentName\";"))
    assert(content.contains("public final static String subjectPrefix = \"https://lod.ehri-project-test.eu/vocabularies/ehri-pers/other-name/\";"))

    assert(content.contains("private List<String> rdfsLabel;"))
    assert(content.contains("private IRIValue id;"))

    val setRdfsLabel = "public RicoAgentName1 setRdfsLabel\\(List<String> rdfsLabel\\)[ \r\n]*[{][ \r\n]*this.rdfsLabel = rdfsLabel;[ \r\n]*return this;[ \r\n]*[}]".r
    val setId = "public RicoAgentName1 setId\\(IRIValue id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setRdfsLabel.findFirstIn(content).isDefined)
    assert(setId.findFirstIn(content).isDefined)

    val getRdfsLabel = "public List<String> getRdfsLabel\\(\\)[ \r\n]*[{][ \r\n]*return this.rdfsLabel;[ \r\n]*[}]".r
    val getId = "public IRIValue getId\\(\\)[ \r\n]*[{][ \r\n]*return this.id;[ \r\n]*[}]".r

    assert(getRdfsLabel.findFirstIn(content).isDefined)
    assert(getId.findFirstIn(content).isDefined)
  }

  test("Class 2 is correctly generated") {
    val content = loadClass("RicoAgentName2")
    assert(content.contains("public class RicoAgentName2"))
    assert(content.contains("package com.example;"))

    assert(content.contains("public final static String rdfType = \"https://www.ica.org/standards/RiC/ontology#AgentName\";"))
    assert(content.contains("public final static String subjectPrefix = \"https://lod.ehri-project-test.eu/vocabularies/ehri-pers/parallel-name/\";"))

    assert(content.contains("private List<String> rdfsLabel;"))
    assert(content.contains("private IRIValue id;"))

    val setRdfsLabel = "public RicoAgentName2 setRdfsLabel\\(List<String> rdfsLabel\\)[ \r\n]*[{][ \r\n]*this.rdfsLabel = rdfsLabel;[ \r\n]*return this;[ \r\n]*[}]".r
    val setId = "public RicoAgentName2 setId\\(IRIValue id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setRdfsLabel.findFirstIn(content).isDefined)
    assert(setId.findFirstIn(content).isDefined)

    val getRdfsLabel = "public List<String> getRdfsLabel\\(\\)[ \r\n]*[{][ \r\n]*return this.rdfsLabel;[ \r\n]*[}]".r
    val getId = "public IRIValue getId\\(\\)[ \r\n]*[{][ \r\n]*return this.id;[ \r\n]*[}]".r

    assert(getRdfsLabel.findFirstIn(content).isDefined)
    assert(getId.findFirstIn(content).isDefined)
  }

  test("Service class 1 is correctly generated") {
    val content = loadClass("RicoAgentName1Service")
    assert(content.contains("public class RicoAgentName1Service"))
    assert(content.contains("package com.example;"))

    assert(content.contains("private DataAccess dataAccess;"))

    val constructor = "public RicoAgentName1Service[(][)][ \r\n]*[{][ \r\n]*this.dataAccess = DataAccessSingleton.getInstance[(][)];[ \r\n]*[}]".r
    assert(constructor.findFirstIn(content).isDefined)

    val getAll = "public List<RicoAgentName1> getAll\\(\\)[ \r\n]*[{][ \r\n]*return dataAccess.getAll[(]RicoAgentName1.class[)];[ \r\n]*[}]".r
    val getAllRDF = "public String getAll\\(String rdfFormat\\)[ \r\n]*[{][ \r\n]*return dataAccess.getAll[(]RicoAgentName1.class, rdfFormat[)];[ \r\n]*[}]".r
    val getById = "public Optional<RicoAgentName1> getById[(]String id[)][ \r\n]*[{][ \r\n]*return dataAccess.getById[(]RicoAgentName1\\.class, id[)];[ \r\n]*[}]".r
    val getByIdRDF = "public String getById[(]String id, String rdfFormat[)][ \r\n]*[{][ \r\n]*return dataAccess.getById[(]RicoAgentName1\\.class, id, rdfFormat[)];[ \r\n]*[}]".r
    val getByField = "public List<RicoAgentName1> getByField[(]String fieldName, String value[)][ \r\n]*[{][ \r\n]*return dataAccess.getByField[(]RicoAgentName1\\.class, fieldName, value[)];[ \r\n]*[}]".r
    val getByFieldRDF = "public String getByField[(]String fieldName, String value, String rdfFormat[)][ \r\n]*[{][ \r\n]*return dataAccess.getByField[(]RicoAgentName1\\.class, fieldName, value, rdfFormat[)];[ \r\n]*[}]".r
    assert(getAll.findFirstIn(content).isDefined)
    assert(getById.findFirstIn(content).isDefined)
    assert(getByField.findFirstIn(content).isDefined)
    assert(getAllRDF.findFirstIn(content).isDefined)
    assert(getByIdRDF.findFirstIn(content).isDefined)
    assert(getByFieldRDF.findFirstIn(content).isDefined)
  }

  test("Service class 2 is correctly generated") {
    val content = loadClass("RicoAgentName2Service")
    assert(content.contains("public class RicoAgentName2Service"))
    assert(content.contains("package com.example;"))

    assert(content.contains("private DataAccess dataAccess;"))

    val constructor = "public RicoAgentName2Service[(][)][ \r\n]*[{][ \r\n]*this.dataAccess = DataAccessSingleton.getInstance[(][)];[ \r\n]*[}]".r
    assert(constructor.findFirstIn(content).isDefined)

    val getAll = "public List<RicoAgentName2> getAll\\(\\)[ \r\n]*[{][ \r\n]*return dataAccess.getAll[(]RicoAgentName2.class[)];[ \r\n]*[}]".r
    val getAllRDF = "public String getAll\\(String rdfFormat\\)[ \r\n]*[{][ \r\n]*return dataAccess.getAll[(]RicoAgentName2.class, rdfFormat[)];[ \r\n]*[}]".r
    val getById = "public Optional<RicoAgentName2> getById[(]String id[)][ \r\n]*[{][ \r\n]*return dataAccess.getById[(]RicoAgentName2\\.class, id[)];[ \r\n]*[}]".r
    val getByIdRDF = "public String getById[(]String id, String rdfFormat[)][ \r\n]*[{][ \r\n]*return dataAccess.getById[(]RicoAgentName2\\.class, id, rdfFormat[)];[ \r\n]*[}]".r
    val getByField = "public List<RicoAgentName2> getByField[(]String fieldName, String value[)][ \r\n]*[{][ \r\n]*return dataAccess.getByField[(]RicoAgentName2\\.class, fieldName, value[)];[ \r\n]*[}]".r
    val getByFieldRDF = "public String getByField[(]String fieldName, String value, String rdfFormat[)][ \r\n]*[{][ \r\n]*return dataAccess.getByField[(]RicoAgentName2\\.class, fieldName, value, rdfFormat[)];[ \r\n]*[}]".r
    assert(getAll.findFirstIn(content).isDefined)
    assert(getById.findFirstIn(content).isDefined)
    assert(getByField.findFirstIn(content).isDefined)
    assert(getAllRDF.findFirstIn(content).isDefined)
    assert(getByIdRDF.findFirstIn(content).isDefined)
    assert(getByFieldRDF.findFirstIn(content).isDefined)
  }

}
