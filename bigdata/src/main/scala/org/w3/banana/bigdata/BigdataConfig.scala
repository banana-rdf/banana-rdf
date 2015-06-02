package org.w3.banana.bigdata

import java.util.Properties


trait BigdataConfig
{
  def journal:String
  def basePrefix:String
  def properties:Properties
}

object BigdataConfig extends BigdataConfig{

  //I know that it is terrible shared mutable stay but there is no other way to conifugure basestr and pass it to BigdataRDFStore
  var basePrefix:String = "http://todo.example.com/"
  var journal = "bigdata.jnl"
  /**
  BigData settings
    */
  lazy val properties:Properties = {
    val props = new Properties()
    props.setProperty("com.bigdata.rdf.store.AbstractTripleStore.quadsMode","true")
    props.setProperty("com.bigdata.rdf.store.AbstractTripleStore.textIndex","true")
    props.setProperty("com.bigdata.rdf.store.AbstractTripleStore.storeBlankNodes","true")

    props.setProperty("com.bigdata.rdf.sail.truthMaintenance","false")
    props.setProperty("com.bigdata.rdf.sail.statementIdentifiers","true")
    //props.setProperty("com.bigdata.journal.AbstractJournal.file",journal)
    props
  }

}
