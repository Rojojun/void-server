package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component

@Component
class UnknownCommand : CommandStrategy {
    override suspend fun execute(context: CommandContext): CommandResult =
        CommandResult.unknown(
            command = context.command
        )

    override fun getIntent(): CommandIntent = CommandIntent.UNKNOWN

    override fun getSupportedCommands(): List<String> = emptyList()

    override fun matches(command: String): Boolean = true

    override fun getHelp(): String = """
        Unknown command
        
        'help' 명령어를(을) 사용하여 유효한 커맨드를 확인하세요.
    """.trimIndent()
}