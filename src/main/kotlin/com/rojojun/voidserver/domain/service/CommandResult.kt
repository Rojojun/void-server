package com.rojojun.voidserver.domain.service

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import java.util.UUID

/**
 * Command 실행 결과
 *
 * 리눅스 명령어 실행 결과를 모델링
 */
data class CommandResult(
    val output: String,
    val exitCode: Int = 0,
    val error: String? = null,
    val intent: CommandIntent? = null
) {
    /**
     * 명령어 실행 성공 여부
     */
    val isSuccess: Boolean
        get() = exitCode == 0 && error == null

    companion object {
        /**
         * 성공 결과 생성
         */
        fun success(output: String, intent: CommandIntent? = null): CommandResult {
            return CommandResult(
                output = output,
                exitCode = 0,
                intent = intent
            )
        }

        /**
         * 실패 결과 생성
         */
        fun failure(error: String, exitCode: Int = 1): CommandResult {
            return CommandResult(
                output = "",
                exitCode = exitCode,
                error = "[ERROR] $error"
            )
        }

        /**
         * 알 수 없는 명령어
         */
        fun unknown(command: String): CommandResult {
            return CommandResult(
                output = "",
                exitCode = 127,
                error = "[ERROR] Unknown command: $command",
                intent = CommandIntent.UNKNOWN
            )
        }
    }
}

fun CommandResult.toCommand(sessionId: Long, originalCommand: String): Command =
    Command(
        sessionId = sessionId,
        command = originalCommand,
        response = if (this.isSuccess) this.output else (this.error ?: "Unknown error"),
        intent = this.intent,
    )