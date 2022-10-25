package com.herminiogarcia.dmaog.dataAccess.generatedCode;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;

import java.util.HashMap;
import java.util.Map;

public class DataAccessSingleton {

    private static DataAccess dataAccess;
    private static String dataFile = "src/test/resources/data.ttl";
    private static String mappingRules;
    private static String mappingLanguage;
    private static Long reloadMinutes;
    private static String username;
    private static String password;
    private static String drivers = "";
    private static String sparqlEndpoint;
    private static String sparqlEndpointUsername;
    private static String sparqlEndpointPassword;
    private static Map<String, String> prefixes = new HashMap<String, String>();

    public static DataAccess getInstance() {
        if(dataAccess == null) {
            dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes,
                    username, password, drivers, sparqlEndpoint, sparqlEndpointUsername, sparqlEndpointPassword, prefixes);
        }
        return dataAccess;
    }
}
