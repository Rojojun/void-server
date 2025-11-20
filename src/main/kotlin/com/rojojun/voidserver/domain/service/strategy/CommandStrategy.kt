package com.rojojun.voidserver.domain.service.strategy

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult

/**
 * Command Strategy Interface
 *
 * Strategy Pattern + Command Pattern
 * 각 CommandIntent별로 실행 로직을 캡슐화
 */
interface CommandStrategy {

    /**
     * 명령어 실행
     */
    suspend fun execute(context: CommandContext): CommandResult

    /**
     * 이 전략이 지원하는 CommandIntent
     */
    fun getIntent(): CommandIntent

    /**
     * 명령어 매칭 여부 확인
     */
    fun matches(command: String): Boolean {
        // 기본 구현: 정확히 일치하는 경우만
        return getSupportedCommands().any { it.equals(command, ignoreCase = true) }
    }

    /**
     * 지원하는 명령어 목록
     */
    fun getSupportedCommands(): List<String>

    /**
     * 도움말 메시지
     */
    fun getHelp(): String {
        return "Usage: ${getSupportedCommands().joinToString(", ")}"
    }
}
