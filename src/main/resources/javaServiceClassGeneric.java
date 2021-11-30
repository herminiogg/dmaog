package $package;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;

public class $className {

    private DataAccess dataAccess;

    public $className(String dataFile, String mappingRules, Long reloadMinutes) {
        this.dataAccess = new DataAccess(dataFile, mappingRules, reloadMinutes);
    }

    public List<$type> getAll() {
        return dataAccess.getAll($type.class);
    }

}