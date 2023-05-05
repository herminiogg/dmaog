package com.herminiogarcia.dmaog.common

import org.apache.jena.query.{QueryExecutionFactory, QueryFactory, ResultSet, ResultSetFactory}
import org.apache.jena.rdf.model.Model
import com.typesafe.scalalogging.Logger

object QueryExecutorFactory {
  def getQueryExecutor(sparql: String, sparqlEndpoint: Option[String], modelLoader: () => Model): QueryExecutor = sparqlEndpoint match {
    case Some(se) => SparqlEndpointQueryExecutor(sparql, se)
    case None => LocalFileQueryExecutor(modelLoader(), sparql)
  }
}

sealed trait QueryExecutor {
  val sparql: String

  def execute(): ResultSet
}

case class LocalFileQueryExecutor(model: Model, sparql: String) extends QueryExecutor {
  
  val logger = Logger[LocalFileQueryExecutor]
  
  def execute(): ResultSet = {
    logger.info("Executing SPARQL query against local file")
    logger.debug(sparql)
    val query = QueryFactory.create(sparql)
    val queryExecution = QueryExecutionFactory.create(query, model)
    val resultSet = ResultSetFactory.copyResults(queryExecution.execSelect())
    queryExecution.close()
    resultSet
  }
}

case class SparqlEndpointQueryExecutor(sparql: String, endpoint: String) extends QueryExecutor {

  val logger = Logger[SparqlEndpointQueryExecutor]
  
  def execute(): ResultSet = {
    logger.info(s"Executing SPARQL query against endpoint $endpoint")
    logger.debug(sparql)
    val query = QueryFactory.create(sparql)
    val queryExecution = QueryExecutionFactory.sparqlService(endpoint, query)
    val resultSet = ResultSetFactory.copyResults(queryExecution.execSelect())
    queryExecution.close()
    resultSet
  }
}
