package com.github.niqdev
package model

import eu.timepit.refined.types.string.NonEmptyString

case class Settings(environment: NonEmptyString)
