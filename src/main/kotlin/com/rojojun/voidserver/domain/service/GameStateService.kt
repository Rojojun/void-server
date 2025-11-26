package com.rojojun.voidserver.domain.service

import com.rojojun.voidserver.domain.model.EndingType
import com.rojojun.voidserver.domain.model.GameState
import com.rojojun.voidserver.domain.port.out.LoadGameStatePort
import com.rojojun.voidserver.domain.port.out.SaveGameStatePort
import org.springframework.stereotype.Service

@Service
class GameStateService(
    private val loadGameStatePort: LoadGameStatePort,
    private val saveGameStatePort: SaveGameStatePort
) {
    suspend fun getSession(sessionId: Long): GameState {
        return loadGameStatePort.loadSession(sessionId)
            ?: createNewSession(sessionId)
    }

    private suspend fun createNewSession(id: Long): GameState {
        val newState = GameState(
            id = id,
            act = 1,
            logicSealBroken = false,
            dataSealBroken = false,
            powerSealBroken = false,
            endingType = null
        )
        return saveGameStatePort.saveSession(newState)
    }

    suspend fun updateLogicSeal(sessionId: Long, broken: Boolean) {
        updateSession(sessionId) { it.copy(logicSealBroken = broken) }
    }

    suspend fun updateDataSeal(sessionId: Long, broken: Boolean) {
        updateSession(sessionId) { it.copy(dataSealBroken = broken) }
    }

    suspend fun updatePowerSeal(sessionId: Long, broken: Boolean) {
        updateSession(sessionId) { it.copy(powerSealBroken = broken) }
    }

    suspend fun updateEnding(sessionId: Long, endingType: EndingType) {
        updateSession(sessionId) { it.copy(endingType = endingType) }
    }
    
    suspend fun updateAct(sessionId: Long, act: Int) {
        updateSession(sessionId) { it.copy(act = act) }
    }

    private suspend fun updateSession(sessionId: Long, update: (GameState) -> GameState) {
        val currentState = loadGameStatePort.loadSession(sessionId)
            ?: throw IllegalStateException("Session not found: $sessionId")
        
        val updatedState = update(currentState)
        saveGameStatePort.saveSession(updatedState)
    }
}
