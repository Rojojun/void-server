package com.rojojun.voidserver.domain.service.strategy.impl

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.service.CommandContext
import com.rojojun.voidserver.domain.service.CommandResult
import com.rojojun.voidserver.domain.service.strategy.CommandStrategy
import org.springframework.stereotype.Component
import java.io.File

/**
 * List Files Command Strategy
 *
 * 리눅스 'ls' 명령어 구현
 * 파일 목록 조회
 */
@Component
class ListFilesCommand : CommandStrategy {

    override suspend fun execute(context: CommandContext): CommandResult {
        return try {
            val directory = context.args.firstOrNull() ?: context.workingDirectory
            val path = resolvePath(context.workingDirectory, directory)
            val file = File(path)

            if (!file.exists()) {
                return CommandResult.failure("No such file or directory: $directory", exitCode = 2)
            }

            if (!file.isDirectory) {
                return CommandResult.success(file.name, intent = getIntent())
            }

            val files = file.listFiles()?.sortedBy { it.name } ?: emptyList()

            // -la 옵션 확인
            val isLongFormat = context.args.contains("-l") || context.args.contains("-la")
            val showHidden = context.args.contains("-a") || context.args.contains("-la")

            val filteredFiles = if (showHidden) files else files.filter { !it.name.startsWith(".") }

            val output = if (isLongFormat) {
                formatLongList(filteredFiles)
            } else {
                filteredFiles.joinToString("\n") { it.name }
            }

            CommandResult.success(output, intent = getIntent())
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
            File(workingDir, path).canonicalPath
        }
    }

    /**
     * Long format 출력 생성 (ls -l)
     */
    private fun formatLongList(files: List<File>): String {
        return files.joinToString("\n") { file ->
            val permissions = getPermissions(file)
            val size = if (file.isDirectory) "DIR" else file.length().toString()
            val name = file.name
            "$permissions  $size  $name"
        }
    }

    /**
     * 파일 권한 문자열 생성
     */
    private fun getPermissions(file: File): String {
        val type = if (file.isDirectory) "d" else "-"
        val read = if (file.canRead()) "r" else "-"
        val write = if (file.canWrite()) "w" else "-"
        val execute = if (file.canExecute()) "x" else "-"
        return "$type$read$write$execute------"
    }
}