package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Execute Script Command Strategy
 *
 * 스크립트 실행 명령어 (bash, sh 등)
 * 보안상 제한적으로 구현
 */
@Component
class ExecuteScriptCommand : CommandStrategy {

    companion object {
        private const val TIMEOUT_SECONDS = 30L
        private val ALLOWED_COMMANDS = setOf("echo", "date", "pwd", "whoami")
    }

    override suspend fun execute(context: CommandContext): CommandResult {
        if (context.args.isEmpty()) {
            return CommandResult.failure("Usage: exec <command>", exitCode = 1)
        }

        val scriptCommand = context.args.joinToString(" ")

        // 보안: 허용된 명령어만 실행
        val command = context.args.first()
        if (!ALLOWED_COMMANDS.contains(command)) {
            return CommandResult.failure(
                "Command not allowed: $command. Allowed: ${ALLOWED_COMMANDS.joinToString()}",
                exitCode = 126
            )
        }

        return try {
            executeSystemCommand(scriptCommand, context.workingDirectory)
        } catch (e: Exception) {
            CommandResult.failure("Error executing script: ${e.message}")
        }
    }

    override fun getIntent(): CommandIntent = CommandIntent.EXECUTE_SCRIPT

    override fun getSupportedCommands(): List<String> = listOf("exec", "bash", "sh")

    override fun getHelp(): String = """
        exec <command> [args...]

        Execute system command (limited for security)

        Allowed commands: ${ALLOWED_COMMANDS.joinToString()}

        Examples:
          exec echo "Hello World"
          exec date
          exec pwd
    """.trimIndent()

    /**
     * 시스템 명령어 실행
     */
    private suspend fun executeSystemCommand(command: String, workingDir: String): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                val processBuilder = ProcessBuilder("/bin/sh", "-c", command)
                    .directory(File(workingDir))
                    .redirectErrorStream(true)

                val process = processBuilder.start()

                val completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)

                if (!completed) {
                    process.destroy()
                    return@withContext CommandResult.failure("Command timeout after ${TIMEOUT_SECONDS}s", exitCode = 124)
                }

                val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
                val exitCode = process.exitValue()

                if (exitCode == 0) {
                    CommandResult.success(output.trim(), intent = getIntent())
                } else {
                    CommandResult.failure(output.trim(), exitCode = exitCode)
                }
            } catch (e: Exception) {
                CommandResult.failure("Execution error: ${e.message}")
            }
        }
    }
}