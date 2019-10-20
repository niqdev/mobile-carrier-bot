package com.github.niqdev

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

package object model {

  // TODO example
  type ApiToken = String Refined MatchesRegex[W.`"[a-zA-Z0-9]{25,40}"`.T]

}
