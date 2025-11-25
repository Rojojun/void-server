package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component

@Component
class KillProcessCommand : CommandStrategy {

    override suspend fun execute(context: CommandContext): CommandResult {
        if (context.args.isEmpty()) {
            return CommandResult.failure(
                error = "Usage: kill <pid>",
                exitCode = 1
            )
        }

        val pid = context.args.first()

        return CommandResult.success(
            output = "Kill signal to process: $pid",
            intent = getIntent()
        )
    }

    override fun getIntent(): CommandIntent = CommandIntent.KILL_PROCESS

    override fun getSupportedCommands(): List<String> = listOf("kill", "terminate")

    override fun getHelp(): String = """
        Kill <pid>
        
        pid의 신호를 종료시킵니다.
        
        Examples:
            kill 1234
    """.trimIndent()
}