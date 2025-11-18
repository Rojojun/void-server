package com.rojojun.voidserver.domain.port.out

import com.rojojun.voidserver.domain.model.Command
import java.util.UUID

/**
 * Outbound Port - 명령어 히스토리 저장소
 *
 * 도메인 계층의 인터페이스 (프레임워크 독립적)
 * 어댑터 계층에서 구현
 */
interface CommandHistoryRepository {

    /**
     * 명령어 저장
     */
    suspend fun save(command: Command): Command

    /**
     * ID로 조회
     */
    suspend fun findById(id: Long): Command?

    /**
     * 세션의 전체 명령어 히스토리 조회
     */
    suspend fun findBySessionId(sessionId: UUID): List<Command>

    /**
     * 세션의 최근 명령어 조회
     */
    suspend fun findRecentBySessionId(sessionId: UUID, limit: Int = 10): List<Command>

    /**
     * 세션의 명령어 개수
     */
    suspend fun countBySessionId(sessionId: UUID): Long
}
