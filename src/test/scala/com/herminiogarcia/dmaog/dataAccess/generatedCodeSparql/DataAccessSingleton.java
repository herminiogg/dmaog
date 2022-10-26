package com.herminiogarcia.dmaog.dataAccess.generatedCodeSparql;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;

import java.util.HashMap;
import java.util.Map;

public class DataAccessSingleton {

    private static DataAccess dataAccess;
    private static String dataFile = null;
    private static String mappingRules;
    private static String mappingLanguage;
    private static Long reloadMinutes;
    private static String username;
    private static String password;
    private static String drivers = "";
    private static String sparqlEndpoint = "http://localhost:3030/example";
    private static String sparqlEndpointUsername = "root";
    private static String sparqlEndpointPassword = "root";
    private static Map<String, String> prefixes = new HashMap<String, String>() {{
        put("dbr","http://dbpedia.org/resource/");
        put("xsd","http://www.w3.org/2001/XMLSchema#");
        put("schema","http://schema.org/");
        put("","http://example.com/");
    }};

    public static DataAccess getInstance() {
        if(dataAccess == null) {
            dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes,
                    username, password, drivers, sparqlEndpoint, sparqlEndpointUsername, sparqlEndpointPassword, prefixes);
        }
        return dataAccess;
    }
}
