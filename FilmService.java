package com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;

public class FilmService {

    private DataAccess dataAccess;

    public FilmService(String dataFile) {
        this.dataAccess = new DataAccess(dataFile);
    }

    public List<Film> getAll() {
        return dataAccess.getAll(Film.class);
    }

}