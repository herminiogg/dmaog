package com.herminiogarcia.dmaog.dataAccess.generatedCodeSparql;

import com.herminiogarcia.dmaog.common.IRIValue;
import com.herminiogarcia.dmaog.common.MultilingualString;

import java.util.List;

public class Film {

    public final static String rdfType = "http://example.com/Film";
    public final static String subjectPrefix = "http://example.com/";

    private String schemaName;
    private IRIValue schemaMusicBy;
    private IRIValue schemaDirector;
    private IRIValue schemaCountryOfOrigin;
    private Integer year;
    private List<IRIValue> screenwritter;
    private MultilingualString nameWithLanguage;
    private IRIValue cinematographer;
    private IRIValue id;

    public Film() {

    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public IRIValue getSchemaMusicBy() {
        return this.schemaMusicBy;
    }

    public IRIValue getSchemaDirector() {
        return this.schemaDirector;
    }

    public IRIValue getSchemaCountryOfOrigin() {
        return this.schemaCountryOfOrigin;
    }

    public Integer getYear() {
        return this.year;
    }

    public List<IRIValue> getScreenwritter() {
        return this.screenwritter;
    }

    public MultilingualString getNameWithLanguage() {
        return this.nameWithLanguage;
    }

    public IRIValue getCinematographer() {
        return this.cinematographer;
    }

    public IRIValue getId() {
        return this.id;
    }


    public Film setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public Film setSchemaMusicBy(IRIValue schemaMusicBy) {
        this.schemaMusicBy = schemaMusicBy;
        return this;
    }

    public Film setSchemaDirector(IRIValue schemaDirector) {
        this.schemaDirector = schemaDirector;
        return this;
    }

    public Film setSchemaCountryOfOrigin(IRIValue schemaCountryOfOrigin) {
        this.schemaCountryOfOrigin = schemaCountryOfOrigin;
        return this;
    }

    public Film setYear(Integer year) {
        this.year = year;
        return this;
    }

    public Film setScreenwritter(List<IRIValue> screenwritter) {
        this.screenwritter = screenwritter;
        return this;
    }

    public Film setNameWithLanguage(MultilingualString nameWithLanguage) {
        this.nameWithLanguage = nameWithLanguage;
        return this;
    }

    public Film setCinematographer(IRIValue cinematographer) {
        this.cinematographer = cinematographer;
        return this;
    }

    public Film setId(IRIValue id) {
        this.id = id;
        return this;
    }


}