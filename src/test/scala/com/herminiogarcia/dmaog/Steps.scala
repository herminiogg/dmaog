package com.herminiogarcia.dmaog

import com.herminiogarcia.dmaog.dataAccess.{DatesDataAccessTest, FilmDataAccessRDFFromSPARQLEndpointTest, FilmDataAccessRDFTest, FilmDataAccessTest, FilmPaginationTest, LocalPersistanceUpdateTest, SparqlPersistanceAndDataAccessTest, SparqlPersistanceUpdateTest}
import org.scalatest.Sequential

class Steps extends Sequential(
  new FilmCodeGenerationTest,
  new FilmCodeStaticGenerationTest,
  new FilmServiceTest,
  new FilmAndActorCodeGenerationTest,
  new DataAccessSingletonTest,
  new FilmCodeGenerationFromURLDataTest,
  new DateTypesGenerationTest,
  new MultiplePrefixesPerTypeDynamicGenerationTest,
  new MultiplePrefixesPerTypeStaticGenerationTest,

  new FilmDataAccessTest,
  new FilmDataAccessRDFTest,
  new FilmDataAccessRDFFromSPARQLEndpointTest,
  new LocalPersistanceUpdateTest,
  new FilmPaginationTest,
  new SparqlPersistanceAndDataAccessTest,
  new SparqlPersistanceUpdateTest,
  new DatesDataAccessTest)
