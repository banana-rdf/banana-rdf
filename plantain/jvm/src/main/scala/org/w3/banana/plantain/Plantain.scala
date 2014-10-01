package org.w3.banana.plantain

import akka.http.model.Uri

trait Plantain extends org.w3.banana.plantain.generic.Plantain[Uri]
object Plantain extends PlantainModule
