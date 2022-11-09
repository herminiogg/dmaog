package com.herminiogarcia.dmaog.dataAccess.generatedCodeBNode;

import com.herminiogarcia.dmaog.common.IRIValue;
import com.herminiogarcia.dmaog.common.BNode;
import com.herminiogarcia.dmaog.common.MultilingualString;
import java.util.List;

public class Actor {

    public final static String rdfType = "http://example.com/Actor";
    public final static String subjectPrefix = "";

    private String name;
    private IRIValue appear_on;
    private BNode id;

    public Actor() {

    }

    public String getName() {
        return this.name;
    }

    public IRIValue getAppear_on() {
        return this.appear_on;
    }

    public BNode getId() {
        return this.id;
    }


    public Actor setName(String name) {
        this.name = name;
        return this;
    }

    public Actor setAppear_on(IRIValue appear_on) {
        this.appear_on = appear_on;
        return this;
    }

    public Actor setId(BNode id) {
        this.id = id;
        return this;
    }


}