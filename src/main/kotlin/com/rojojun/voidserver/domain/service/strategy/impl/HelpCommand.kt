package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component

/**
 * Help Command Strategy
 *
 * 도움말 출력
 */
@Component
class HelpCommand(
    private val allStrategies: List<CommandStrategy>
) : CommandStrategy {

    override suspend fun execute(context: CommandContext): CommandResult {
        val specificCommand = context.args.firstOrNull()

        val output = if (specificCommand != null) {
            // 특정 명령어 도움말
            val strategy = allStrategies.find { it.matches(specificCommand) }
            strategy?.getHelp() ?: "Unknown command: $specificCommand"
        } else {
            // 전체 명령어 목록
            buildGeneralHelp()
        }

        return CommandResult.success(output, intent = getIntent())
    }

    override fun getIntent(): CommandIntent = CommandIntent.HELP

    override fun getSupportedCommands(): List<String> = listOf("help", "?", "man")

    override fun getHelp(): String = """
        help [command]

        Display help information

        Examples:
          help          Show all available commands
          help ls       Show help for 'ls' command
    """.trimIndent()

    private fun buildGeneralHelp(): String {
        val commands = allStrategies
            .filter { it.getIntent() != CommandIntent.UNKNOWN }
            .groupBy { it.getIntent() }
            .map { (intent, strategies) ->
                val commandList = strategies.flatMap { it.getSupportedCommands() }.joinToString(", ")
                "  ${intent.name.padEnd(20)} $commandList"
            }

        return """
            Available Commands:

            ${commands.joinToString("\n")}

            Use 'help <command>' for more information on a specific command.
        """.trimIndent()
    }
}