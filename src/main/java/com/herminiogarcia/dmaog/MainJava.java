package com.herminiogarcia.dmaog;

import org.apache.jena.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MainJava {

    public static void main(String[] args) {
        /**val shexml = scala.io.Source.fromFile("films.shexml").mkString
         new CodeGenerator(shexml, ".", "com.herminiogarcia.dmaog").generate()
         val resultFilms = new FilmService(".").getAll
         val resultActors = new ActorService(".").getAll
         println(resultFilms)
         println(resultActors)*/
        String rules = null;
        try {
            rules = String.join("", Files.readAllLines(Paths.get("films.shexml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Film> films = new FilmService(".", rules, 2L).getAll();
        List<Actor> actors = new ActorService(".", null, null).getAll();
        System.out.println(films);
        System.out.println(actors);
    }

}
