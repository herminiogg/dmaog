package com.herminiogarcia.com.herminiogarcia.dmaog;

import com.herminiogarcia.com.herminiogarcia.dmaog.Actor;
import com.herminiogarcia.com.herminiogarcia.dmaog.ActorService;
import com.herminiogarcia.com.herminiogarcia.dmaog.Film;
import com.herminiogarcia.com.herminiogarcia.dmaog.FilmService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MainJavaRML {

    public static void main(String[] args) {
        String rules = null;
        try {
            rules = String.join("", Files.readAllLines(Paths.get("films.rml.ttl")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Film> films = new FilmService(".", rules, "rml", 2L).getAll();
        List<Actor> actors = new ActorService(".", null, null, null).getAll();
        System.out.println(films);
        System.out.println(actors);
    }

}
