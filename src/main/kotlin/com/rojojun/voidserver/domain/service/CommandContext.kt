package com.rojojun.voidserver.domain.service

import java.util.UUID

/**
 * Command 실행 컨텍스트
 *
 * 명령어 실행에 필요한 모든 정보를 담고 있음
 */
data class CommandContext(
    val sessionId: Long,
    val command: String,
    val workingDirectory: String = "/",
    val environment: Map<String, String> = emptyMap(),
    val args: List<String> = emptyList()
) {
    companion object {
        /**
         * 명령어 문자열을 파싱하여 CommandContext 생성
         */
        fun parse(sessionId: Long, commandLine: String, workingDir: String = "/"): CommandContext {
            val parts = commandLine.trim().split("\\s+".toRegex())
            val command = parts.firstOrNull() ?: ""
            val args = parts.drop(1)

            return CommandContext(
                sessionId = sessionId,
                command = command,
                workingDirectory = workingDir,
                args = args
            )
        }
    }
}
