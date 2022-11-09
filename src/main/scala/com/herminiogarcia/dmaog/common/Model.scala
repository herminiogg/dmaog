package com.herminiogarcia.dmaog.common


class IRIValue(val iri: String, val namespace: String, val localName: String)
class BNode(val value: String)
class MultilingualString(val value: String, val langTag: String)
class DataTypedPredicate(val predicate: String, val dataType: String)
