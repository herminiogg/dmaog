package com.herminiogarcia.dmaog.common

import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.RDFDataMgr

trait ModelLoader {

  protected def loadModel(pathToRDF: String): Model = {
    RDFDataMgr.loadModel(pathToRDF)
  }

}
