package com.herminiogarcia.dmaog

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfter, DoNotDiscover}

@DoNotDiscover
class DateTypesGenerationTest extends AnyFunSuite with BeforeAndAfter with ClassGenerator {

  val rules =
    """
      |PREFIX : <http://example.com/>
      |PREFIX dbr: <http://dbpedia.org/resource/>
      |PREFIX schema: <http://schema.org/>
      |PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
      |SOURCE films_xml_file <https://shexml.herminiogarcia.com/files/films.xml>
      |ITERATOR film_xml <xpath: //film> {
      |    FIELD dummy <dummy>
      |}
      |EXPRESSION films <films_xml_file.film_xml>
      |
      |:TestDataTypes :[films.id] {
      |    a :TestDataTypes ;
      |    :date [films.date] xsd:date ;
      |    :dateTime [films.dateTime] xsd:dateTime ;
      |    :dateTimeStamp [films.dateTimeStamp] xsd:dateTimeStamp ;
      |    :time [films.time] xsd:time ;
      |    :year [films.year] xsd:gYear ;
      |    :month [films.month] xsd:gMonth ;
      |    :yearMonth [films.yearMonth] xsd:gYearMonth ;
      |    :monthDay [films.monthDay] xsd:gMonthDay ;
      |    :day [films.day] xsd:gDay ;
      |}
      |""".stripMargin

  before {
    generateClasses(true)
  }

  test("Types for attributes are correctly generated") {
    val content = loadClass("TestDataTypes")
    assert(content.contains("private List<java.time.LocalDate> date;"))
    assert(content.contains("private List<java.time.LocalDateTime> dateTime;"))
    assert(content.contains("private List<java.time.ZonedDateTime> dateTimeStamp;"))
    assert(content.contains("private List<java.time.LocalTime> time;"))
    assert(content.contains("private List<java.time.Year> year;"))
    assert(content.contains("private List<String> month;"))
    assert(content.contains("private List<java.time.YearMonth> yearMonth;"))
    assert(content.contains("private List<java.time.MonthDay> monthDay;"))
    assert(content.contains("private List<String> day;"))
  }

}
