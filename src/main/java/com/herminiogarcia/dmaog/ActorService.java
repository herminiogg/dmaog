package com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;

public class ActorService {

    private DataAccess dataAccess;

    public ActorService(String dataFile) {
        this.dataAccess = new DataAccess(dataFile);
    }

    public List<Actor> getAll() {
        return dataAccess.getAll(Actor.class);
    }

}