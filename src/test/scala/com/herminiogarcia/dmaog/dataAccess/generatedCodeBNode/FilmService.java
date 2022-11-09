package com.herminiogarcia.dmaog.dataAccess.generatedCodeBNode;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;
import java.util.Optional;

public class FilmService {

    private DataAccess dataAccess;

    public FilmService() {
        this.dataAccess = DataAccessSingleton.getInstance();
    }

    public List<Film> getAll() {
        return dataAccess.getAll(Film.class);
    }

    public String getAll(String rdfFormat) {
        return dataAccess.getAll(Film.class, rdfFormat);
    }

    public List<Film> getAll(Long limit, Long offset) {
        return dataAccess.getAll(Film.class, limit, offset);
    }

    public Long count() {
        return dataAccess.count(Film.class);
    }

    public Optional<Film> getById(String id) {
        return dataAccess.getById(Film.class, id);
    }

    public String getById(String id, String rdfFormat) {
        return dataAccess.getById(Film.class, id, rdfFormat);
    }

    public List<Film> getByField(String fieldName, String value) {
        return dataAccess.getByField(Film.class, fieldName, value);
    }

    public String getByField(String fieldName, String value, String rdfFormat) {
        return dataAccess.getByField(Film.class, fieldName, value, rdfFormat);
    }

    public void commit(Film instance) {
        dataAccess.delete(instance);
        dataAccess.insert(instance);
    }

    public void delete(Film instance) {
        dataAccess.delete(instance);
    }

}