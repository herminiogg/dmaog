package com.herminiogarcia.dmaog.dataAccess

import com.herminiogarcia.dmaog.dataAccess.generatedCode.FilmService
import com.herminiogarcia.dmaog.dataAccess.generatedCodeDate.TestDataTypesService
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfter, DoNotDiscover}

import java.text.SimpleDateFormat
import java.time.{DayOfWeek, LocalDate, LocalDateTime, LocalTime, Month, MonthDay, Year, YearMonth, ZonedDateTime}
import java.util.Date
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@DoNotDiscover
class DatesDataAccessTest extends AnyFunSuite with RDFStatementCreator {

  val testDataTypesService = new TestDataTypesService()

  test("getAll is returning all members") {
    testData()
  }

  test("Update data and test that everything is persisted in the same way") {
    val item = testDataTypesService.getById("1").get()
    testDataTypesService.commit(item)
    testData()
  }

  def testData(): Unit = {
    val item = testDataTypesService.getById("1").get()

    assert(item.getDate.get(0).equals(LocalDate.of(2022, 11, 04)))

    assert(item.getDateTime.get(0).equals(LocalDateTime.of(2022, 11, 04, 15, 07, 02)))

    val date = ZonedDateTime.parse("2022-11-04T15:07:02Z")
    assert(item.getDateTimeStamp.get(0).equals(date))

    assert(item.getTime.get(0).equals(LocalTime.of(23, 02, 01)))

    assert(item.getYear.get(0).equals(Year.of(2022)))

    assert(item.getMonth.get(0).equals("--11"))

    assert(item.getYearMonth.get(0).equals(YearMonth.of(2022, 11)))

    assert(item.getMonthDay.get(0).equals(MonthDay.of(11, 04)))

    assert(item.getDay.get(0).equals("---04"))
  }

}
