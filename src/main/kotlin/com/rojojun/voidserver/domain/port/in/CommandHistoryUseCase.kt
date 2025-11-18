package com.rojojun.voidserver.domain.port.`in`

import com.rojojun.voidserver.domain.model.Command
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Inbound Port - 명령어 히스토리 유스케이스
 *
 * 애플리케이션의 진입점 (비즈니스 로직 인터페이스)
 * Coroutine Flow 기반 Reactive Programming
 */
interface CommandHistoryUseCase {

    /**
     * 명령어 저장
     * @return 저장된 Command (ID 포함)
     */
    suspend fun saveCommand(command: Command): Command

    /**
     * 특정 명령어 조회
     * @return Command 또는 null
     */
    suspend fun getCommandById(id: Long): Command?

    /**
     * 세션의 전체 명령어 히스토리 조회 (Reactive Stream)
     * @return Flow of Commands
     */
    fun getSessionHistory(sessionId: UUID): Flow<Command>

    /**
     * 세션의 최근 N개 명령어 조회 (Reactive Stream)
     * @return Flow of recent Commands
     */
    fun getRecentCommands(sessionId: UUID, limit: Int = 10): Flow<Command>

    /**
     * 세션의 명령어 개수
     * @return 명령어 카운트
     */
    suspend fun getCommandCount(sessionId: UUID): Long

    /**
     * 명령어 검색 (command 텍스트 기반)
     * @return Flow of matching Commands
     */
    fun searchCommands(sessionId: UUID, keyword: String): Flow<Command>
}
