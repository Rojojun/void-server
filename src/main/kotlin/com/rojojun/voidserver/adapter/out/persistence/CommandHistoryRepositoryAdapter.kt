package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.adapter.out.persistence.entity.CommandEntity
import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.port.out.CommandHistoryRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Outbound Adapter - 명령어 히스토리 저장소 구현
 *
 * Domain Port를 R2DBC로 구현
 * Coroutine 기반 비동기 처리
 */
@Component
class CommandHistoryRepositoryAdapter(
    private val r2dbcRepository: R2dbcCommandRepository
) : CommandHistoryRepository {

    override suspend fun save(command: Command): Command {
        val entity = CommandEntity.from(command)
        val saved = r2dbcRepository.save(entity)
        return saved.toDomain()
    }

    override suspend fun findById(id: Long): Command? {
        return r2dbcRepository.findById(id)?.toDomain()
    }

    override suspend fun findBySessionId(sessionId: UUID): List<Command> {
        return r2dbcRepository.findBySessionIdOrderByTimestampDesc(sessionId)
            .map { it.toDomain() }
    }

    override suspend fun findRecentBySessionId(sessionId: UUID, limit: Int): List<Command> {
        return r2dbcRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId)
            .take(limit)
            .map { it.toDomain() }
    }

    override suspend fun countBySessionId(sessionId: UUID): Long {
        return r2dbcRepository.countBySessionId(sessionId)
    }
}
