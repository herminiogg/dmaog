package com.herminiogarcia.dmaog.dataAccess.generatedCodeBNode;

import com.herminiogarcia.dmaog.common.IRIValue;
import com.herminiogarcia.dmaog.common.BNode;
import com.herminiogarcia.dmaog.common.MultilingualString;
import java.util.List;

public class Film {

    public final static String rdfType = "http://example.com/Film";
    public final static String subjectPrefix = "http://example.com/";

    private String schemaName;
    private List<BNode> schemaActor;
    private Integer year;
    private IRIValue id;

    public Film() {

    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public List<BNode> getSchemaActor() {
        return this.schemaActor;
    }

    public Integer getYear() {
        return this.year;
    }

    public IRIValue getId() {
        return this.id;
    }


    public Film setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public Film setSchemaActor(List<BNode> schemaActor) {
        this.schemaActor = schemaActor;
        return this;
    }

    public Film setYear(Integer year) {
        this.year = year;
        return this;
    }

    public Film setId(IRIValue id) {
        this.id = id;
        return this;
    }


}