package $package;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;

public class $className {

    private DataAccess dataAccess;

    public $className(String dataFile) {
        this.dataAccess = new DataAccess(dataFile);
    }

    public List<$type> getAll() {
        return dataAccess.getAll($type.class);
    }

}