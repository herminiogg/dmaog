package com.herminiogarcia.dmaog.dataAccess.generatedCodeBNode;

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

    public String getAll(String rdfFormat) {
        return dataAccess.getAll(Actor.class, rdfFormat);
    }

    public List<Actor> getAll(Long limit, Long offset) {
        return dataAccess.getAll(Actor.class, limit, offset);
    }

    public Long count() {
        return dataAccess.count(Actor.class);
    }

    public Optional<Actor> getById(String id) {
        return dataAccess.getById(Actor.class, id);
    }

    public String getById(String id, String rdfFormat) {
        return dataAccess.getById(Actor.class, id, rdfFormat);
    }

    public List<Actor> getByField(String fieldName, String value) {
        return dataAccess.getByField(Actor.class, fieldName, value);
    }

    public String getByField(String fieldName, String value, String rdfFormat) {
        return dataAccess.getByField(Actor.class, fieldName, value, rdfFormat);
    }

    public void commit(Actor instance) {
        dataAccess.delete(instance);
        dataAccess.insert(instance);
    }

    public void delete(Actor instance) {
        dataAccess.delete(instance);
    }

}