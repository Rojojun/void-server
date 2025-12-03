package com.rojojun.voidserver.domain.service

import com.rojojun.voidserver.domain.model.GameState
import com.rojojun.voidserver.domain.port.out.LoadGameStatePort
import com.rojojun.voidserver.domain.port.out.SaveGameStatePort
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class GameStateServiceTest : BehaviorSpec({
    val loadGameStatePort = mockk<LoadGameStatePort>()
    val saveGameStatePort = mockk<SaveGameStatePort>()

    val gameStateService = GameStateService(loadGameStatePort, saveGameStatePort)

    Given("새로운 세션 ID가 주어졌을 때") {
        val sessionId = 1L

        coEvery { loadGameStatePort.loadSession(sessionId) } returns null

        val newGameState = GameState(
            id = sessionId,
            act = 1,
            logicSealBroken = false,
            dataSealBroken = false,
            powerSealBroken = false,
            endingType = null,
        )

        coEvery { saveGameStatePort.saveSession(any()) } returns newGameState

        When("getSession을 호출하면") {
            val result = gameStateService.getSession(sessionId)

            Then("새로운 세션이 생성되고 저장된다") {
                result.id shouldBe sessionId
                result.powerSealBroken shouldBe false
            }

            And("SaveGameStatePort는 호출되지 않는다") {
                coVerify(exactly = 1) { saveGameStatePort.saveSession(any()) }
            }
        }
    }

    Given("기존 세션 ID가 주어졌을 때") {
        val sessionId = 2L
        val currentState = GameState(
            id = sessionId,
            act = 1,
            logicSealBroken = false,
            dataSealBroken = false,
            powerSealBroken = false,
            endingType = null
        )

        coEvery { loadGameStatePort.loadSession(sessionId) } returns currentState

        coEvery { saveGameStatePort.saveSession(any()) } answers { firstArg() }

        When("봉인 해제 요청 (updateLogicSeal(broken=true))을 하면") {
            gameStateService.updateLogicSeal(sessionId, true)

            Then("SaveGameStatePort가 호출된다") {
                coVerify(exactly = 1) { saveGameStatePort.saveSession(any()) }
            }

            And("상태는 true로 저장된다") {
                coVerify(exactly = 1) { saveGameStatePort.saveSession(match { gameState ->
                    gameState.id == sessionId && gameState.logicSealBroken
                }) }
            }
        }
    }
})