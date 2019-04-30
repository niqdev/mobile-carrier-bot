package com.github.niqdev
package service

import com.github.ghik.silencer.silent
import com.github.niqdev.repository.TelegramRepository

@silent
sealed abstract class TelegramService[F[_], D](repository: TelegramRepository[F, D]) {}
