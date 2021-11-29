package com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.common.IRIValue;

public class Actor {

    public final static String rdfType = "http://example.com/Actor";

    private String name;
	private IRIValue appear_on;
	private IRIValue id;

    public Actor() {

    }

    public String getName() {
        return this.name;
    }

	public IRIValue getAppear_on() {
        return this.appear_on;
    }

	public IRIValue getId() {
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

	public Actor setId(IRIValue id) {
        this.id = id;
        return this;
    }


}