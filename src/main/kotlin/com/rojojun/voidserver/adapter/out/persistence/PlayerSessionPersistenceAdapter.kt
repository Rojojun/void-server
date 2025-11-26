package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.adapter.out.persistence.entity.PlayerSessionEntity
import com.rojojun.voidserver.domain.model.EndingType
import com.rojojun.voidserver.domain.model.GameState
import com.rojojun.voidserver.domain.port.out.LoadGameStatePort
import com.rojojun.voidserver.domain.port.out.SaveGameStatePort
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PlayerSessionPersistenceAdapter(
    private val playerSessionRepository: PlayerSessionRepository
) : LoadGameStatePort, SaveGameStatePort {

    override suspend fun loadSession(sessionId: Long): GameState? {
        return playerSessionRepository.findById(sessionId)
            ?.toDomain()
    }

    override suspend fun saveSession(gameState: GameState): GameState {
        val entity = gameState.toEntity()
        return playerSessionRepository.save(entity).toDomain()
    }

    private fun PlayerSessionEntity.toDomain(): GameState {
        return GameState(
            id = this.id!!,
            act = this.act,
            logicSealBroken = this.logicSealBroken,
            dataSealBroken = this.dataSealBroken,
            powerSealBroken = this.powerSealBroken,
            endingType = this.endingType?.let { EndingType.valueOf(it) }
        )
    }

    private fun GameState.toEntity(): PlayerSessionEntity {
        return PlayerSessionEntity(
            id = this.id,
            act = this.act,
            logicSealBroken = this.logicSealBroken,
            dataSealBroken = this.dataSealBroken,
            powerSealBroken = this.powerSealBroken,
            endingType = this.endingType?.name,
            lastActiveAt = Instant.now()
        )
    }
}
