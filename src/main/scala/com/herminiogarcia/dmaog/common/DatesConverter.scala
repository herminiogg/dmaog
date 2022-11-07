package com.herminiogarcia.dmaog.common

import java.util.Date
import java.text.SimpleDateFormat
import java.time.{LocalDate, LocalDateTime, LocalTime, Month, MonthDay, Year, YearMonth, ZoneId, ZonedDateTime}

object DatesConverter {

  def isDate(dataType: String): Boolean = {
    dataType == "java.time.LocalDate" ||
    dataType == "java.time.LocalDateTime" ||
    dataType == "java.time.ZonedDateTime" ||
    dataType == "java.time.LocalTime" ||
    dataType == "java.time.Year" ||
    dataType == "java.time.Month" ||
    dataType == "java.time.YearMonth" ||
    dataType == "java.time.MonthDay"
  }

  def convertDateToJavaDate(dataType: String, value: String): Object = {
    if(dataType == "java.time.LocalDateTime") {
      LocalDateTime.parse(value)
    } else if(dataType == "java.time.LocalDate") {
      LocalDate.parse(value)
    } else if(dataType == "java.time.ZonedDateTime") {
      ZonedDateTime.parse(value)
    } else if(dataType == "java.time.LocalTime") {
      LocalTime.parse(value)
    } else if(dataType == "java.time.YearMonth") {
      YearMonth.parse(value)
    } else if(dataType == "java.time.Year") {
      Year.parse(value)
    } else if(dataType == "java.time.MonthDay") {
      MonthDay.parse(value)
    } else {
      throw new Exception(s"Impossible to convert type $dataType to Java datetime format")
    }
  }

}
