package com.herminiogarcia.dmaog.common

import org.apache.jena.query.{QueryExecutionFactory, QueryFactory, ResultSet, ResultSetFactory}
import org.apache.jena.rdf.model.Model

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
  def execute(): ResultSet = {
    val query = QueryFactory.create(sparql)
    val queryExecution = QueryExecutionFactory.create(query, model)
    val resultSet = ResultSetFactory.copyResults(queryExecution.execSelect())
    queryExecution.close()
    resultSet
  }
}

case class SparqlEndpointQueryExecutor(sparql: String, endpoint: String) extends QueryExecutor {
  def execute(): ResultSet = {
    val query = QueryFactory.create(sparql)
    val queryExecution = QueryExecutionFactory.sparqlService(endpoint, query)
    val resultSet = ResultSetFactory.copyResults(queryExecution.execSelect())
    queryExecution.close()
    resultSet
  }
}
