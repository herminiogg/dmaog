package $package;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;
import java.util.Optional;

public class $className {

    private DataAccess dataAccess;

    public $className() {
        this.dataAccess = DataAccessSingleton.getInstance();
    }

    public List<$type> getAll() {
        return dataAccess.getAll($type.class);
    }

    public String getAll(String rdfFormat) {
        return dataAccess.getAll($type.class, rdfFormat);
    }

    public Optional<$type> getById(String id) {
        return dataAccess.getById($type.class, id);
    }

    public String getById(String id, String rdfFormat) {
        return dataAccess.getById($type.class, id, rdfFormat);
    }

    public List<$type> getByField(String fieldName, String value) {
        return dataAccess.getByField($type.class, fieldName, value);
    }

    public String getByField(String fieldName, String value, String rdfFormat) {
        return dataAccess.getByField($type.class, fieldName, value, rdfFormat);
    }

}