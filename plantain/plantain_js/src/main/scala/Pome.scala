package org.w3.banana.pome

import java.net.{ URI => jURI }

trait Pome extends org.w3.banana.plantain.generic.Plantain[jURI]

object Pome extends PomeModule
