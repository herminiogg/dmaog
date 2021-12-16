package com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;
import java.util.Optional;

public class ActorService {

    private DataAccess dataAccess;

    public ActorService(String dataFile, String mappingRules, String mappingLanguage, Long reloadMinutes, String username, String password) {
        String drivers = "algo%algo;otro%otro";
        this.dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes, username, password, drivers);
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