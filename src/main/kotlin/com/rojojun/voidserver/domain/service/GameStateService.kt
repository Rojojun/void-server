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
            // Act 1 초기 상태
            elaraContacted = false,
            hiddenDirectoryFound = false,
            systemLogRead = false,
            secureDirectoryAccessed = false,
            // Act 2 초기 상태
            logicSealBroken = false,
            dataSealBroken = false,
            powerSealBroken = false,
            // Act 3 초기 상태
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

    // Act 1 상태 업데이트
    suspend fun updateElaraContacted(sessionId: Long, contacted: Boolean) {
        updateSession(sessionId) { it.copy(elaraContacted = contacted) }
    }

    suspend fun updateHiddenDirectoryFound(sessionId: Long, found: Boolean) {
        updateSession(sessionId) { it.copy(hiddenDirectoryFound = found) }
    }

    suspend fun updateSystemLogRead(sessionId: Long, read: Boolean) {
        updateSession(sessionId) { it.copy(systemLogRead = read) }
    }

    suspend fun updateSecureDirectoryAccessed(sessionId: Long, accessed: Boolean) {
        updateSession(sessionId) { it.copy(secureDirectoryAccessed = accessed) }
    }

    private suspend fun updateSession(sessionId: Long, update: (GameState) -> GameState) {
        val currentState = loadGameStatePort.loadSession(sessionId)
            ?: throw IllegalStateException("Session not found: $sessionId")

        val updatedState = update(currentState)
        saveGameStatePort.saveSession(updatedState)
    }
}
