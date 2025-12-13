package com.rojojun.voidserver.domain.service

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Service
import java.util.TreeSet
import java.util.UUID

@Service
class CommandActor(
    private val strategies: List<CommandStrategy>
) {
    suspend fun execute(sessionId: UUID, commandLine: String, workingDirectory: String = "/"): CommandResult =
        CommandContext.parse(
            sessionId = sessionId,
            commandLine = commandLine,
            workingDir = workingDirectory
        ).run { findStrategy(this.command).execute(this) }

    suspend fun executeAndBuildCommand(sessionId: UUID, commandLine: String, workingDirectory: String = "/"): Command =
        execute(
            sessionId = sessionId,
            commandLine = commandLine,
            workingDirectory = workingDirectory
        ).toCommand(
            sessionId = sessionId,
            originalCommand = commandLine,
        )

    fun getAvailableCommands(): List<String> =
        strategies
            .filter { it.getIntent() != CommandIntent.UNKNOWN }
            .flatMapTo(TreeSet()) { it.getSupportedCommands() }
            .toList()

    fun getCommandsByIntent(): CommandIntentAlias =
        strategies
            .filter { it.getIntent() != CommandIntent.UNKNOWN }
            .groupBy { it.getIntent() }
            .mapValues { (_, strategies) ->
                strategies.flatMapTo(TreeSet()) { it.getSupportedCommands() }.toList()
            }

    fun getHelp(command: String? = null): String =
        if (command != null) {
            val parsedCommand = command.trim().split("\\s+".toRegex()).firstOrNull() ?: ""
            findStrategy(parsedCommand).getHelp()
        } else {
            buildGeneralHelp()
        }

    private fun findStrategy(commandLine: String): CommandStrategy =
        strategies
            .firstOrNull {
                it.getIntent() != CommandIntent.UNKNOWN && it.matches(commandLine)
            } ?: strategies.first { it.getIntent() == CommandIntent.UNKNOWN }

    private fun buildGeneralHelp(): String {
        TODO("Not yet implemented")
    }
}

typealias CommandIntentAlias = Map<CommandIntent, List<String>>