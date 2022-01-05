package $package;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;

public class DataAccessSingleton {

    private static DataAccess dataAccess;
    private static String dataFile = "$pathToData/data.ttl";
    private static String mappingRules;
    private static String mappingLanguage;
    private static Long reloadMinutes;
    private static String username;
    private static String password;
    private static String drivers = "$drivers";

    public static DataAccess getInstance() {
        if(dataAccess == null) {
            dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes, username, password, drivers);
        }
        return dataAccess;
    }
}
