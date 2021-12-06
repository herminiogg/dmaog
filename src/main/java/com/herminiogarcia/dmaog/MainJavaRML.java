package com.herminiogarcia.com.herminiogarcia.dmaog;

import com.herminiogarcia.com.herminiogarcia.dmaog.Actor;
import com.herminiogarcia.com.herminiogarcia.dmaog.ActorService;
import com.herminiogarcia.com.herminiogarcia.dmaog.Film;
import com.herminiogarcia.com.herminiogarcia.dmaog.FilmService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class MainJavaRML {

    public static void main(String[] args) {
        String rules = null;
        try {
            rules = String.join("", Files.readAllLines(Paths.get("films.rml.ttl")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FilmService filmService = new FilmService(".", rules, "rml", 2L);
        ActorService actorService = new ActorService(".", null, null, null);
        List<Film> films = filmService.getAll();
        List<Actor> actors = actorService.getAll();
        Optional<Film> film2 = filmService.getById("2");
        Optional<Actor> actorCaine = actorService.getById("Michael%20Caine");
        List<Film> filmsUSA = filmService.getByField("schemaName", "Interstellar");
        List<Actor> actorBale = actorService.getByField("name", "Christian Bale");

        System.out.println(films);
        System.out.println(actors);
    }

}
