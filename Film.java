package com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.common.IRIValue;
import java.util.List;

public class Film {

    public final static String rdfType = "http://example.com/Film";
    public final static String subjectPrefix = "http://example.com/";

    private Integer year;
    private List<IRIValue> schemaActor;
    private List<IRIValue> screenwritter;
    private String schemaName;
    private IRIValue schemaMusicBy;
    private IRIValue schemaCountryOfOrigin;
    private IRIValue schemaDirector;
    private IRIValue id;

    public Film() {

    }

    public Integer getYear() {
        return this.year;
    }

    public List<IRIValue> getSchemaActor() {
        return this.schemaActor;
    }

    public List<IRIValue> getScreenwritter() {
        return this.screenwritter;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public IRIValue getSchemaMusicBy() {
        return this.schemaMusicBy;
    }

    public IRIValue getSchemaCountryOfOrigin() {
        return this.schemaCountryOfOrigin;
    }

    public IRIValue getSchemaDirector() {
        return this.schemaDirector;
    }

    public IRIValue getId() {
        return this.id;
    }


    public Film setYear(Integer year) {
        this.year = year;
        return this;
    }

    public Film setSchemaActor(List<IRIValue> schemaActor) {
        this.schemaActor = schemaActor;
        return this;
    }

    public Film setScreenwritter(List<IRIValue> screenwritter) {
        this.screenwritter = screenwritter;
        return this;
    }

    public Film setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public Film setSchemaMusicBy(IRIValue schemaMusicBy) {
        this.schemaMusicBy = schemaMusicBy;
        return this;
    }

    public Film setSchemaCountryOfOrigin(IRIValue schemaCountryOfOrigin) {
        this.schemaCountryOfOrigin = schemaCountryOfOrigin;
        return this;
    }

    public Film setSchemaDirector(IRIValue schemaDirector) {
        this.schemaDirector = schemaDirector;
        return this;
    }

    public Film setId(IRIValue id) {
        this.id = id;
        return this;
    }


}