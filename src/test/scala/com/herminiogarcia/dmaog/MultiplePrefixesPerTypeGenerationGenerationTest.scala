package com.herminiogarcia.dmaog

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfter, DoNotDiscover}

@DoNotDiscover
class MultiplePrefixesPerTypeGenerationGenerationTest extends AnyFunSuite with BeforeAndAfter with ClassGenerator {

  override val rules = null

  before {
    generateClasses(pathToData = Option("ehriPers.ttl"))
  }

  test("Class 1 is correctly generated") {
    val content = loadClass("RicoAgentName1")
    assert(content.contains("public class RicoAgentName1"))
    assert(content.contains("package com.example;"))

    assert(content.contains("public final static String rdfType = \"https://www.ica.org/standards/RiC/ontology#AgentName\";"))
    assert(content.contains("public final static String subjectPrefix = \"https://lod.ehri-project-test.eu/vocabularies/ehri-pers/other-name/\";"))

    assert(content.contains("private String rdfsLabel;"))
    assert(content.contains("private IRIValue id;"))

    val setRdfsLabel = "public RicoAgentName1 setRdfsLabel\\(String rdfsLabel\\)[ \r\n]*[{][ \r\n]*this.rdfsLabel = rdfsLabel;[ \r\n]*return this;[ \r\n]*[}]".r
    val setId = "public RicoAgentName1 setId\\(IRIValue id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setRdfsLabel.findFirstIn(content).isDefined)
    assert(setId.findFirstIn(content).isDefined)

    val getRdfsLabel = "public String getRdfsLabel\\(\\)[ \r\n]*[{][ \r\n]*return this.rdfsLabel;[ \r\n]*[}]".r
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

    assert(content.contains("private String rdfsLabel;"))
    assert(content.contains("private IRIValue id;"))

    val setRdfsLabel = "public RicoAgentName2 setRdfsLabel\\(String rdfsLabel\\)[ \r\n]*[{][ \r\n]*this.rdfsLabel = rdfsLabel;[ \r\n]*return this;[ \r\n]*[}]".r
    val setId = "public RicoAgentName2 setId\\(IRIValue id\\)[ \r\n]*[{][ \r\n]*this.id = id;[ \r\n]*return this;[ \r\n]*[}]".r
    assert(setRdfsLabel.findFirstIn(content).isDefined)
    assert(setId.findFirstIn(content).isDefined)

    val getRdfsLabel = "public String getRdfsLabel\\(\\)[ \r\n]*[{][ \r\n]*return this.rdfsLabel;[ \r\n]*[}]".r
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
