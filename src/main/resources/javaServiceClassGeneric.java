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

    public Optional<$type> getById(String id) {
        return dataAccess.getById($type.class, id);
    }

    public List<$type> getByField(String fieldName, String value) {
        return dataAccess.getByField($type.class, fieldName, value);
    }

}