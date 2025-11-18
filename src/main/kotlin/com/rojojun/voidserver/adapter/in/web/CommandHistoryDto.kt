package com.rojojun.voidserver.adapter.`in`.web

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import java.time.Instant
import java.util.UUID

/**
 * Command History API DTOs
 *
 * Web Layer의 데이터 전송 객체들
 * Domain Model과 분리하여 API 스펙 변경에 유연하게 대응
 */

/**
 * 명령어 저장 요청 DTO
 */
data class SaveCommandRequest(
    val sessionId: String,
    val command: String,
    val response: String,
    val intent: String? = null
) {
    /**
     * Domain Model로 변환
     */
    fun toDomain(): Command = Command(
        sessionId = UUID.fromString(sessionId),
        command = command,
        response = response,
        intent = intent?.let { CommandIntent.valueOf(it) }
    )
}

/**
 * 명령어 응답 DTO
 */
data class CommandResponse(
    val id: Long?,
    val sessionId: String,
    val command: String,
    val response: String,
    val intent: String?,
    val timestamp: Instant
) {
    companion object {
        /**
         * Domain Model에서 변환
         */
        fun from(command: Command): CommandResponse = CommandResponse(
            id = command.id,
            sessionId = command.sessionId.toString(),
            command = command.command,
            response = command.response,
            intent = command.intent?.name,
            timestamp = command.timestamp
        )
    }
}

/**
 * 세션 통계 응답 DTO
 */
data class SessionStatsResponse(
    val sessionId: UUID,
    val totalCommands: Long,
    val recentCommands: List<CommandResponse>
)

/**
 * 명령어 목록 응답 DTO (페이징 지원)
 */
data class CommandListResponse(
    val commands: List<CommandResponse>,
    val total: Long,
    val hasMore: Boolean = false
)

/**
 * 에러 응답 DTO
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Instant = Instant.now()
)
