package org.w3.banana

import org.w3.banana.plantain._

package object experimental {

  type PlantainScript[+A] = scalaz.Free[({ type l[+x] = LDPCommand[Plantain, x] })#l, A]

}
