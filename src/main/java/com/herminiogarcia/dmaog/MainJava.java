package com.herminiogarcia.dmaog;

import com.herminiogarcia.com.herminiogarcia.dmaog.Actor;
import com.herminiogarcia.com.herminiogarcia.dmaog.ActorService;
import com.herminiogarcia.com.herminiogarcia.dmaog.Film;
import com.herminiogarcia.com.herminiogarcia.dmaog.FilmService;
import org.apache.jena.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MainJava {

    public static void main(String[] args) throws InterruptedException {
        String rules = null;
        try {
            rules = String.join("", Files.readAllLines(Paths.get("films.shexml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Film> films = new FilmService().getAll();
        List<Actor> actors = new ActorService().getAll();
        System.out.println(films);
        Thread.sleep(10000L);
        System.out.println(actors);
    }

}
