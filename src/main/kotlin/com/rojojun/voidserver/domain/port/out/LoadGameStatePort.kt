package com.rojojun.voidserver.domain.port.out

import com.rojojun.voidserver.domain.model.GameState

interface LoadGameStatePort {
    suspend fun loadSession(sessionId: Long): GameState?
}
