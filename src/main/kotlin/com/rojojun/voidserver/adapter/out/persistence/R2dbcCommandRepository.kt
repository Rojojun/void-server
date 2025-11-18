package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.adapter.out.persistence.entity.CommandEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * R2DBC 리포지토리 - Coroutine 기반
 *
 * Kotlin R2DBC 표준:
 * - CoroutineCrudRepository 사용 (suspend fun 지원)
 * - ReactiveCrudRepository 대신 Coroutine 사용
 */
@Repository
interface R2dbcCommandRepository : CoroutineCrudRepository<CommandEntity, Long> {

    /**
     * 세션 ID로 명령어 히스토리 조회
     */
    suspend fun findBySessionIdOrderByTimestampDesc(sessionId: UUID): List<CommandEntity>

    /**
     * 세션 ID로 최근 N개 명령어 조회
     */
    suspend fun findTop10BySessionIdOrderByTimestampDesc(sessionId: UUID): List<CommandEntity>

    /**
     * 세션의 명령어 개수
     */
    suspend fun countBySessionId(sessionId: UUID): Long
}
