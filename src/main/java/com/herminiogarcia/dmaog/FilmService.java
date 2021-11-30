package com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;

public class FilmService {

    private DataAccess dataAccess;

    public FilmService(String dataFile, String mappingRules, Long reloadMinutes) {
        this.dataAccess = new DataAccess(dataFile, mappingRules, reloadMinutes);
    }

    public List<Film> getAll() {
        return dataAccess.getAll(Film.class);
    }

}