package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.model.GameEvent
import com.rojojun.voidserver.domain.model.GameEventType
import com.rojojun.voidserver.domain.port.out.GameEventPort
import com.rojojun.voidserver.domain.port.out.VirtualFileSystemPort
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component

/**
 * Run Script Command Strategy
 *
 * 게임 스크립트 파일 실행
 * connect.sh 실행 시 Elara 첫 만남 이벤트 트리거
 */
@Component
class RunScriptCommand(
    private val fileSystem: VirtualFileSystemPort,
    private val eventPort: GameEventPort
) : CommandStrategy {

    override suspend fun execute(context: CommandContext): CommandResult {
        if (context.args.isEmpty()) {
            return CommandResult.failure("Usage: run <script>", exitCode = 1)
        }

        return try {
            val scriptPath = context.args.first()
            val path = resolvePath(context.workingDirectory, scriptPath)

            // 파일 존재 여부 확인
            if (!fileSystem.exists(context.sessionId, path)) {
                return CommandResult.failure("No such file: $scriptPath", exitCode = 1)
            }

            // 실행 권한 확인
            if (!fileSystem.isExecutable(context.sessionId, path)) {
                return CommandResult.failure("Permission denied: $scriptPath is not executable", exitCode = 126)
            }

            // connect.sh 실행 시 Elara 첫 만남 이벤트
            if (path == "/connect.sh") {
                // 이미 이벤트가 발생했는지 확인
                if (eventPort.hasEventOccurred(context.sessionId, GameEventType.ELARA_FIRST_CONTACT)) {
                    // 이미 만났을 경우 간단한 메시지만
                    return CommandResult.success(
                        "[Elara]: You're back. Please, help me get out of here.",
                        intent = CommandIntent.EXECUTE_SCRIPT
                    )
                }

                // 첫 만남 이벤트 발생
                val event = GameEvent(GameEventType.ELARA_FIRST_CONTACT)
                val messages = eventPort.handleEvent(context.sessionId, event)

                return CommandResult.success(messages, intent = CommandIntent.EXECUTE_SCRIPT)
            }

            // 기타 스크립트는 단순 실행
            val file = fileSystem.readFile(context.sessionId, path)
            val output = file?.content ?: "Script executed: $scriptPath"
            CommandResult.success(output, intent = CommandIntent.EXECUTE_SCRIPT)
        } catch (e: Exception) {
            CommandResult.failure("Error executing script: ${e.message}")
        }
    }

    override fun getIntent(): CommandIntent = CommandIntent.EXECUTE_SCRIPT

    override fun getSupportedCommands(): List<String> = listOf("run", "./")

    override fun getHelp(): String = """
        run <script>

        Execute a script file

        Examples:
          run connect.sh
          run /path/to/script.sh
          ./connect.sh
    """.trimIndent()

    private fun resolvePath(workingDir: String, path: String): String {
        // "./" 제거
        val normalizedPath = if (path.startsWith("./")) path.substring(2) else path
        return if (normalizedPath.startsWith("/")) {
            normalizedPath
        } else {
            "$workingDir/$normalizedPath".replace("//", "/")
        }
    }
}
