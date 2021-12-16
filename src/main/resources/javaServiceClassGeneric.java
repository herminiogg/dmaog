package $package;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;
import java.util.Optional;

public class $className {

    private DataAccess dataAccess;

    public $className(String dataFile, String mappingRules, String mappingLanguage, Long reloadMinutes, String username, String password) {
        String drivers = "$driversString";
        this.dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes, username, password, drivers);
    }

    public List<$type> getAll() {
        return dataAccess.getAll($type.class);
    }

    public Optional<$type> getById(String id) {
        return dataAccess.getById($type.class, id);
    }

    public List<$type> getByField(String fieldName, String value) {
        return dataAccess.getByField($type.class, fieldName, value);
    }

}