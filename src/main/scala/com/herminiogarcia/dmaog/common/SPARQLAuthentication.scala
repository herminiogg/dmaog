package com.herminiogarcia.dmaog.common

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.apache.jena.riot.web.HttpOp

import java.net.URI

trait SPARQLAuthentication {

  def initAuthenticationContext(sparqlEndpoint: Option[String], username: Option[String], password: Option[String]) = {
    //To use in the future when using Jena >= 4.2.0
    /**if(sparqlEndpoint.isDefined && username.isDefined && password.isDefined)
      AuthEnv.get().registerUsernamePassword(URI.create(sparqlEndpoint.get), username.get, password.get)*/

    if(sparqlEndpoint.isDefined && username.isDefined && password.isDefined) {
      val credsProvider = new BasicCredentialsProvider()
      val scopedCredentials = new UsernamePasswordCredentials(username.get, password.get)

      val uri = new URI(sparqlEndpoint.get)
      val host = uri.getHost
      val port = uri.getPort
      val authscope = new AuthScope(host, port, null, null)
      credsProvider.setCredentials(authscope, scopedCredentials)

      val httpclient = HttpClients.custom.setDefaultCredentialsProvider(credsProvider).build()
      HttpOp.setDefaultHttpClient(httpclient)
    }
  }

}
