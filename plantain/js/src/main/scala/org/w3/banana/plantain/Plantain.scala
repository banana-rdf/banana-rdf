package org.w3.banana.plantain

import java.net.{ URI => jURI }

trait Plantain extends org.w3.banana.plantain.generic.Plantain[jURI]

object Plantain extends PlantainModule
