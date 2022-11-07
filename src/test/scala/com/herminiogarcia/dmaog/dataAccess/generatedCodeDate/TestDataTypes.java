package com.herminiogarcia.dmaog.dataAccess.generatedCodeDate;

import com.herminiogarcia.dmaog.common.IRIValue;
import com.herminiogarcia.dmaog.common.MultilingualString;
import java.util.List;

public class TestDataTypes {

    public final static String rdfType = "http://example.com/TestDataTypes";
    public final static String subjectPrefix = "http://example.com/";

    private List<java.time.LocalDate> date;
    private List<java.time.LocalDateTime> dateTime;
    private List<java.time.ZonedDateTime> dateTimeStamp;
    private List<java.time.LocalTime> time;
    private List<java.time.Year> year;
    private List<String> month;
    private List<java.time.YearMonth> yearMonth;
    private List<java.time.MonthDay> monthDay;
    private List<String> day;
    private IRIValue id;

    public TestDataTypes() {

    }

    public List<java.time.LocalDate> getDate() {
        return this.date;
    }

    public List<java.time.LocalDateTime> getDateTime() {
        return this.dateTime;
    }

    public List<java.time.ZonedDateTime> getDateTimeStamp() {
        return this.dateTimeStamp;
    }

    public List<java.time.LocalTime> getTime() {
        return this.time;
    }

    public List<java.time.Year> getYear() {
        return this.year;
    }

    public List<String> getMonth() {
        return this.month;
    }

    public List<java.time.YearMonth> getYearMonth() {
        return this.yearMonth;
    }

    public List<java.time.MonthDay> getMonthDay() {
        return this.monthDay;
    }

    public List<String> getDay() {
        return this.day;
    }

    public IRIValue getId() {
        return this.id;
    }


    public TestDataTypes setDate(List<java.time.LocalDate> date) {
        this.date = date;
        return this;
    }

    public TestDataTypes setDateTime(List<java.time.LocalDateTime> dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public TestDataTypes setDateTimeStamp(List<java.time.ZonedDateTime> dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
        return this;
    }

    public TestDataTypes setTime(List<java.time.LocalTime> time) {
        this.time = time;
        return this;
    }

    public TestDataTypes setYear(List<java.time.Year> year) {
        this.year = year;
        return this;
    }

    public TestDataTypes setMonth(List<String> month) {
        this.month = month;
        return this;
    }

    public TestDataTypes setYearMonth(List<java.time.YearMonth> yearMonth) {
        this.yearMonth = yearMonth;
        return this;
    }

    public TestDataTypes setMonthDay(List<java.time.MonthDay> monthDay) {
        this.monthDay = monthDay;
        return this;
    }

    public TestDataTypes setDay(List<String> day) {
        this.day = day;
        return this;
    }

    public TestDataTypes setId(IRIValue id) {
        this.id = id;
        return this;
    }


}