package com.herminiogarcia.com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;
import java.util.Optional;

public class FilmService {

    private DataAccess dataAccess;

    public FilmService(String dataFile, String mappingRules, String mappingLanguage, Long reloadMinutes) {
        this.dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes);
    }

    public List<Film> getAll() {
        return dataAccess.getAll(Film.class);
    }

    public Optional<Film> getById(String id) {
        return dataAccess.getById(Film.class, id);
    }

    public List<Film> getByField(String fieldName, String value) {
        return dataAccess.getByField(Film.class, fieldName, value);
    }

}