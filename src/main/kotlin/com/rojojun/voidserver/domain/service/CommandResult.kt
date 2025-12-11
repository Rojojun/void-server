package com.rojojun.voidserver.domain.service

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.model.MessageType
import com.rojojun.voidserver.domain.model.ResponseMessage
import java.util.UUID

/**
 * Command 실행 결과 (확장 버전)
 *
 * 다중 응답 메시지를 지원하여 NPC 대사, 시스템 메시지 등을 포함
 */
data class CommandResult(
    val messages: List<ResponseMessage>,
    val exitCode: Int = 0,
    val intent: CommandIntent? = null
) {
    /**
     * 명령어 실행 성공 여부
     */
    val isSuccess: Boolean
        get() = exitCode == 0

    /**
     * 단일 출력 문자열 (하위 호환성)
     */
    val output: String
        get() = messages.joinToString("\n") { it.text }

    /**
     * 에러 메시지 (하위 호환성)
     */
    val error: String?
        get() = messages.firstOrNull { it.type == MessageType.ERROR }?.text

    companion object {
        /**
         * 성공 결과 생성 (단일 메시지)
         */
        fun success(output: String, intent: CommandIntent? = null): CommandResult {
            return CommandResult(
                messages = listOf(ResponseMessage(MessageType.SYSTEM, output)),
                exitCode = 0,
                intent = intent
            )
        }

        /**
         * 성공 결과 생성 (다중 메시지)
         */
        fun success(messages: List<ResponseMessage>, intent: CommandIntent? = null): CommandResult {
            return CommandResult(
                messages = messages,
                exitCode = 0,
                intent = intent
            )
        }

        /**
         * 실패 결과 생성
         */
        fun failure(error: String, exitCode: Int = 1): CommandResult {
            return CommandResult(
                messages = listOf(ResponseMessage(MessageType.ERROR, "[ERROR] $error")),
                exitCode = exitCode
            )
        }

        /**
         * 알 수 없는 명령어
         */
        fun unknown(command: String): CommandResult {
            return CommandResult(
                messages = listOf(ResponseMessage(MessageType.ERROR, "[ERROR] Unknown command: $command")),
                exitCode = 127,
                intent = CommandIntent.UNKNOWN
            )
        }
    }
}

fun CommandResult.toCommand(sessionId: UUID, originalCommand: String): Command =
    Command(
        sessionId = sessionId,
        command = originalCommand,
        response = if (this.isSuccess) this.output else (this.error ?: "Unknown error"),
        intent = this.intent,
    )