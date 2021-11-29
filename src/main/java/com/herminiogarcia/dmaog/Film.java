package com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.common.IRIValue;

public class Film {

    public final static String rdfType = "http://example.com/Film";

    private Integer year;
	private IRIValue schemaActor;
	private String schemaName;
	private IRIValue screenwritter;
	private IRIValue schemaCountryOfOrigin;
	private IRIValue schemaDirector;
	private IRIValue schemaMusicBy;
	private IRIValue cinematographer;
	private IRIValue id;

    public Film() {

    }

    public Integer getYear() {
        return this.year;
    }

	public IRIValue getSchemaActor() {
        return this.schemaActor;
    }

	public String getSchemaName() {
        return this.schemaName;
    }

	public IRIValue getScreenwritter() {
        return this.screenwritter;
    }

	public IRIValue getSchemaCountryOfOrigin() {
        return this.schemaCountryOfOrigin;
    }

	public IRIValue getSchemaDirector() {
        return this.schemaDirector;
    }

	public IRIValue getSchemaMusicBy() {
        return this.schemaMusicBy;
    }

	public IRIValue getCinematographer() {
        return this.cinematographer;
    }

	public IRIValue getId() {
        return this.id;
    }


    public Film setYear(Integer year) {
        this.year = year;
        return this;
    }

	public Film setSchemaActor(IRIValue schemaActor) {
        this.schemaActor = schemaActor;
        return this;
    }

	public Film setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

	public Film setScreenwritter(IRIValue screenwritter) {
        this.screenwritter = screenwritter;
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

	public Film setSchemaMusicBy(IRIValue schemaMusicBy) {
        this.schemaMusicBy = schemaMusicBy;
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

    @Override
    public String toString() {
        return "Film{" +
                "year=" + year +
                ", schemaActor=" + schemaActor +
                ", schemaName='" + schemaName + '\'' +
                ", screenwritter=" + screenwritter +
                ", schemaCountryOfOrigin=" + schemaCountryOfOrigin +
                ", schemaDirector=" + schemaDirector +
                ", schemaMusicBy=" + schemaMusicBy +
                ", cinematographer=" + cinematographer +
                ", id=" + id +
                '}';
    }
}