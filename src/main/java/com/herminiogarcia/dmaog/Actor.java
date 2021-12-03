package com.herminiogarcia.com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.common.IRIValue;
import java.util.List;

public class Actor {

    public final static String rdfType = "http://example.com/Actor";

    private String name;
    private IRIValue id;

    public Actor() {

    }

    public String getName() {
        return this.name;
    }

    public IRIValue getId() {
        return this.id;
    }


    public Actor setName(String name) {
        this.name = name;
        return this;
    }

    public Actor setId(IRIValue id) {
        this.id = id;
        return this;
    }


}