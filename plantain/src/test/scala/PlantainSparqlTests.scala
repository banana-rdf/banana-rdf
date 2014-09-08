package org.w3.banana.plantain

import org.w3.banana.{ SparqlAnswerJson, SparqlAnswerXml, SparqlGraphTest }
import org.w3.banana.plantain.Plantain._

class PlantainSparqlGraphTestXML extends SparqlGraphTest[Plantain, SparqlAnswerXml]

class PlantainSparqlGraphTestJSON extends SparqlGraphTest[Plantain, SparqlAnswerJson]
