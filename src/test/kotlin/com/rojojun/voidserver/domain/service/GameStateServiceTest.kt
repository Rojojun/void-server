package com.rojojun.voidserver.domain.service

import com.rojojun.voidserver.domain.port.out.LoadGameStatePort
import com.rojojun.voidserver.domain.port.out.SaveGameStatePort
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.mockk

class GameStateServiceTest : BehaviorSpec({
    val loadGameStatePort = mockk<LoadGameStatePort>()
    val saveGameStatePort = mockk<SaveGameStatePort>()

    val gameStateService = GameStateService(loadGameStatePort, saveGameStatePort)

})