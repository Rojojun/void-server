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
 * List Files Command Strategy
 *
 * 리눅스 'ls' 명령어 구현
 * 가상 파일 시스템을 사용하여 파일 목록 조회
 */
@Component
class ListFilesCommand(
    private val fileSystem: VirtualFileSystemPort,
    private val eventPort: GameEventPort
) : CommandStrategy {

    override suspend fun execute(context: CommandContext): CommandResult {
        return try {
            // 옵션 확인
            val isLongFormat = context.args.contains("-l") || context.args.contains("-la")
            val showHidden = context.args.contains("-a") || context.args.contains("-la")

            // 옵션이 아닌 첫 번째 인자를 디렉토리로 사용
            val directory = context.args.firstOrNull { !it.startsWith("-") } ?: context.workingDirectory
            val path = resolvePath(context.workingDirectory, directory)

            // 파일 존재 여부 확인
            if (!fileSystem.exists(context.sessionId, path)) {
                return CommandResult.failure("No such file or directory: $directory", exitCode = 2)
            }

            // 파일 목록 조회
            val files = fileSystem.listFiles(context.sessionId, path, showHidden)

            if (files.isEmpty()) {
                return CommandResult.success("", intent = getIntent())
            }

            val output = if (isLongFormat) {
                formatLongList(files)
            } else {
                files.joinToString("\n") { it.name }
            }

            val messages = mutableListOf(ResponseMessage(MessageType.SYSTEM, output))

            // 이벤트 체크: /secure 디렉토리 접근
            if (path.contains("/secure") &&
                !eventPort.hasEventOccurred(context.sessionId, GameEventType.SECURE_DIRECTORY_ACCESSED)) {
                val event = GameEvent(GameEventType.SECURE_DIRECTORY_ACCESSED)
                val eventMessages = eventPort.handleEvent(context.sessionId, event)
                messages.addAll(eventMessages)
            }

            // 이벤트 체크: 숨겨진 파일 발견
            if (showHidden && files.any { it.isHidden } &&
                !eventPort.hasEventOccurred(context.sessionId, GameEventType.HIDDEN_DIRECTORY_FOUND)) {
                val event = GameEvent(GameEventType.HIDDEN_DIRECTORY_FOUND)
                val eventMessages = eventPort.handleEvent(context.sessionId, event)
                messages.addAll(eventMessages)
            }

            CommandResult.success(messages, intent = getIntent())
        } catch (e: Exception) {
            CommandResult.failure("Error listing files: ${e.message}")
        }
    }

    override fun getIntent(): CommandIntent = CommandIntent.LIST_FILES

    override fun getSupportedCommands(): List<String> = listOf("ls", "list", "dir")

    override fun getHelp(): String = """
        ls [OPTIONS] [DIRECTORY]

        List directory contents

        Options:
          -l        Use a long listing format
          -a        Show hidden files
          -la       Long listing format with hidden files

        Examples:
          ls
          ls -la
          ls /path/to/directory
    """.trimIndent()

    /**
     * 상대 경로를 절대 경로로 변환
     */
    private fun resolvePath(workingDir: String, path: String): String {
        return if (path.startsWith("/")) {
            path
        } else {
            "$workingDir/$path".replace("//", "/")
        }
    }

    /**
     * Long format 출력 생성 (ls -l)
     */
    private fun formatLongList(files: List<com.rojojun.voidserver.domain.model.VirtualFile>): String {
        return files.joinToString("\n") { file ->
            val size = if (file.isDirectory) "DIR" else file.size.toString()
            "${file.permissions}  $size  ${file.name}"
        }
    }
}