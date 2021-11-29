package com.herminiogarcia.dmaog

import com.herminiogarcia.dmaog.codeGeneration.CodeGenerator


object Main {

  def main(args: Array[String]): Unit = {
    val shexml = scala.io.Source.fromFile("films.shexml").mkString
    new CodeGenerator(shexml, ".", "com.herminiogarcia.dmaog").generate()
    val resultFilms = new FilmService(".").getAll
    val resultActors = new ActorService(".").getAll
    println(resultFilms)
    println(resultActors)
  }

}
