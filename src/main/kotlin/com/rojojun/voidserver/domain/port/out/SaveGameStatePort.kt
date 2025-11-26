package com.rojojun.voidserver.domain.port.out

import com.rojojun.voidserver.domain.model.GameState

interface SaveGameStatePort {
    suspend fun saveSession(gameState: GameState): GameState
}
