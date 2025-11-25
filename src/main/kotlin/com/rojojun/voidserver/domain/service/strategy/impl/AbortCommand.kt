package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component

@Component
class AbortCommand : CommandStrategy {
    override suspend fun execute(context: CommandContext): CommandResult =
        CommandResult.success(
            output = "현재 세션이 사용자에 의해 종료되었습니다.",
            intent = getIntent()
        )

    override fun getIntent(): CommandIntent = CommandIntent.ABORT

    override fun getSupportedCommands(): List<String> = listOf("abort", "exit", "quit", "bye")

    override fun getHelp(): String = """
        abort

        현재 세션 혹은 명령어를 중단시킵니다.

        단축어: exit, quit, bye

        예시:
          abort
          exit
          quit
    """.trimIndent()
}