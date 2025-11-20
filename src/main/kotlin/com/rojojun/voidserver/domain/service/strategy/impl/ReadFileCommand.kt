package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component
import java.io.File

/**
 * Read File Command Strategy
 *
 * 리눅스 'cat' 명령어 구현
 * 파일 내용 읽기
 */
@Component
class ReadFileCommand : CommandStrategy {

    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
    }

    override suspend fun execute(context: CommandContext): CommandResult {
        if (context.args.isEmpty()) {
            return CommandResult.failure("Usage: cat <file>", exitCode = 1)
        }

        return try {
            val filePath = context.args.first()
            val path = resolvePath(context.workingDirectory, filePath)
            val file = File(path)

            if (!file.exists()) {
                return CommandResult.failure("No such file: $filePath", exitCode = 1)
            }

            if (file.isDirectory) {
                return CommandResult.failure("$filePath is a directory", exitCode = 1)
            }

            if (!file.canRead()) {
                return CommandResult.failure("Permission denied: $filePath", exitCode = 1)
            }

            if (file.length() > MAX_FILE_SIZE) {
                return CommandResult.failure(
                    "File too large (max ${MAX_FILE_SIZE / 1024 / 1024}MB): $filePath",
                    exitCode = 1
                )
            }

            val content = file.readText()
            CommandResult.success(content, intent = getIntent())
        } catch (e: Exception) {
            CommandResult.failure("Error reading file: ${e.message}")
        }
    }

    override fun getIntent(): CommandIntent = CommandIntent.READ_FILE

    override fun getSupportedCommands(): List<String> = listOf("cat", "read", "view")

    override fun getHelp(): String = """
        cat <file>

        Concatenate and display file contents

        Examples:
          cat file.txt
          cat /path/to/file.log
    """.trimIndent()

    private fun resolvePath(workingDir: String, path: String): String {
        return if (path.startsWith("/")) {
            path
        } else {
            File(workingDir, path).canonicalPath
        }
    }
}
