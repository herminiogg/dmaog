package com.herminiogarcia.com.herminiogarcia.dmaog;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;

public class ActorService {

    private DataAccess dataAccess;

    public ActorService(String dataFile, String mappingRules, String mappingLanguage, Long reloadMinutes) {
        this.dataAccess = new DataAccess(dataFile, mappingRules, mappingLanguage, reloadMinutes);
    }

    public List<Actor> getAll() {
        return dataAccess.getAll(Actor.class);
    }

}