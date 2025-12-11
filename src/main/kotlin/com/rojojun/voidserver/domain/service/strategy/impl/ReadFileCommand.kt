package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.model.GameEvent
import com.rojojun.voidserver.domain.model.GameEventType
import com.rojojun.voidserver.domain.model.MessageType
import com.rojojun.voidserver.domain.model.ResponseMessage
import com.rojojun.voidserver.domain.port.out.GameEventPort
import com.rojojun.voidserver.domain.port.out.VirtualFileSystemPort
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component

/**
 * Read File Command Strategy
 *
 * 리눅스 'cat' 명령어 구현
 * 가상 파일 시스템을 사용하여 파일 내용 읽기
 */
@Component
class ReadFileCommand(
    private val fileSystem: VirtualFileSystemPort,
    private val eventPort: GameEventPort
) : CommandStrategy {

    override suspend fun execute(context: CommandContext): CommandResult {
        if (context.args.isEmpty()) {
            return CommandResult.failure("Usage: cat <file>", exitCode = 1)
        }

        return try {
            val filePath = context.args.first()
            val path = resolvePath(context.workingDirectory, filePath)

            // 파일 존재 여부 확인
            if (!fileSystem.exists(context.sessionId, path)) {
                return CommandResult.failure("No such file: $filePath", exitCode = 1)
            }

            // 파일 읽기
            val file = fileSystem.readFile(context.sessionId, path)
                ?: return CommandResult.failure("Cannot read file: $filePath", exitCode = 1)

            // 디렉토리 체크
            if (file.isDirectory) {
                return CommandResult.failure("$filePath is a directory", exitCode = 1)
            }

            val messages = mutableListOf(ResponseMessage(MessageType.SYSTEM, file.content))

            // 이벤트 체크: system_log 읽기
            if (path == "/system_log" &&
                !eventPort.hasEventOccurred(context.sessionId, GameEventType.SYSTEM_LOG_READ)) {
                val event = GameEvent(GameEventType.SYSTEM_LOG_READ)
                val eventMessages = eventPort.handleEvent(context.sessionId, event)
                messages.addAll(eventMessages)
            }

            CommandResult.success(messages, intent = getIntent())
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
            "$workingDir/$path".replace("//", "/")
        }
    }
}
