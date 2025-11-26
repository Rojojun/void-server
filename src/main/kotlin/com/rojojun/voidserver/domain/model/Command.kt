package com.rojojun.voidserver.domain.model

import java.time.Instant
import java.util.UUID

/**
 * 도메인 모델 - 명령어
 * 프레임워크 독립적인 순수 도메인 객체
 */
data class Command(
    val id: Long? = null,
    val sessionId: Long,
    val command: String,
    val response: String,
    val intent: CommandIntent? = null,
    val timestamp: Instant = Instant.now()
)

/**
 * 명령어 의도 열거형
 */
enum class CommandIntent {
    LIST_FILES,
    PWD,
    READ_FILE,
    EXECUTE_SCRIPT,
    KILL_PROCESS,
    HELP,
    ABORT,
    UNKNOWN
}
