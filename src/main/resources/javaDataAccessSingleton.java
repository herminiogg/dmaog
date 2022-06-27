package $package;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.HashMap;
import java.util.Map;

public class DataAccessSingleton {

    private static DataAccess dataAccess;
    private static String dataFile = "$pathToData";
    private static String mappingRules;
    private static String mappingLanguage;
    private static Long reloadMinutes;
    private static String username;
    private static String password;
    private static String drivers = "$drivers";
    private static String sparqlEndpoint = $sparqlEndpoint;
    private static Map<String, String> prefixes = new HashMap<String, String>() {{
        $prefixes
    }};

    public static DataAccess getInstance() {
        if(dataAccess == null) {
            dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes,
                    username, password, drivers, sparqlEndpoint, prefixes);
        }
        return dataAccess;
    }
}
