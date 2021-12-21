package com.herminiogarcia.com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;
import java.util.Optional;

public class ActorService {

    private DataAccess dataAccess;

    public ActorService() {
        this.dataAccess = DataAccessSingleton.getInstance();
    }

    public List<Actor> getAll() {
        return dataAccess.getAll(Actor.class);
    }

    public Optional<Actor> getById(String id) {
        return dataAccess.getById(Actor.class, id);
    }

    public List<Actor> getByField(String fieldName, String value) {
        return dataAccess.getByField(Actor.class, fieldName, value);
    }

}