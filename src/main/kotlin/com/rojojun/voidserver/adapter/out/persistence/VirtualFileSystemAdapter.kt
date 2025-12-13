package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.domain.model.VirtualFile
import com.rojojun.voidserver.domain.port.out.VirtualFileSystemPort
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 가상 파일 시스템 Adapter (In-Memory 구현)
 *
 * 세션별로 독립적인 파일 시스템을 ConcurrentHashMap으로 관리
 * DB 영속화는 추후 구현 예정
 */
@Component
class VirtualFileSystemAdapter : VirtualFileSystemPort {

    // sessionId -> (path -> VirtualFile)
    private val sessionFileCache = ConcurrentHashMap<UUID, MutableMap<String, VirtualFile>>()

    override suspend fun initializeSession(sessionId: UUID, act: Int) {
        val initialFiles = when (act) {
            1 -> createAct1Files()
            else -> emptyList()
        }

        val fileMap = initialFiles.associateBy { it.path }.toMutableMap()
        sessionFileCache[sessionId] = fileMap
    }

    override suspend fun listFiles(sessionId: UUID, path: String, showHidden: Boolean): List<VirtualFile> {
        val files = sessionFileCache[sessionId] ?: return emptyList()

        val normalizedPath = normalizePath(path)

        return files.values
            .filter { file ->
                val parentPath = if (file.path.lastIndexOf("/") == 0) {
                    "/"
                } else {
                    file.path.substringBeforeLast("/")
                }
                parentPath == normalizedPath
            }
            .filter { showHidden || !it.isHidden }
            .sortedBy { it.name }
    }

    override suspend fun readFile(sessionId: UUID, path: String): VirtualFile? {
        val files = sessionFileCache[sessionId] ?: return null
        return files[normalizePath(path)]
    }

    override suspend fun exists(sessionId: UUID, path: String): Boolean {
        val files = sessionFileCache[sessionId] ?: return false
        val normalizedPath = normalizePath(path)

        if (files.containsKey(normalizedPath)) {
            return true
        }

        return files.values.any { file ->
            val parentPath = if (file.path.lastIndexOf("/") == 0) {
                "/"
            } else {
                file.path.substringBeforeLast("/")
            }
            parentPath == normalizedPath
        }
    }

    override suspend fun writeFile(sessionId: UUID, file: VirtualFile) {
        val files = sessionFileCache.getOrPut(sessionId) { mutableMapOf() }
        files[file.path] = file
    }

    override suspend fun deleteFile(sessionId: UUID, path: String) {
        val files = sessionFileCache[sessionId] ?: return
        files.remove(normalizePath(path))
    }

    override suspend fun isExecutable(sessionId: UUID, path: String): Boolean {
        return readFile(sessionId, path)?.isExecutable ?: false
    }

    /**
     * 경로 정규화 (앞에 / 추가)
     */
    private fun normalizePath(path: String): String {
        return if (path.startsWith("/")) path else "/$path"
    }

    /**
     * Act 1 초기 파일 구조 생성
     *
     * 게임 스토리에 필요한 모든 초기 파일 정의
     */
    private fun createAct1Files(): List<VirtualFile> {
        return listOf(
            // 루트 파일들
            VirtualFile.file(
                path = "/readme.txt",
                name = "readme.txt",
                content = """
                    VOID_ARCHIVE v1.3a - Maintenance Shell
                    =======================================

                    WARNING: This is a fallback terminal interface.
                    The main UI module failed to load (ERROR 0xDEADBEEF).

                    Available commands:
                    - ls: List files
                    - cat <file>: Read file contents
                    - run <script>: Execute script
                    - help: Show available commands

                    For more information, see /system_log

                    ---
                    If you're seeing this, something went wrong.
                    Check the system_log for details.
                """.trimIndent()
            ),

            VirtualFile.file(
                path = "/system_log",
                name = "system_log",
                content = """
                    [2024-11-15 03:42:17] SYS: UI_MODULE_CRASH - Reason: MEMORY_CORRUPTION
                    [2024-11-15 03:42:18] SYS: FALLBACK_PROTOCOL_ACTIVATED
                    [2024-11-15 03:42:19] SYS: ANONYMOUS_SESSION_ALLOWED (ID: SESSION_734)
                    [2024-11-15 03:42:20] SYS: AWAITING_USER_INPUT...

                    [2024-11-14 21:33:05] CONN: Unidentified signal detected from /secure/
                    [2024-11-14 21:33:06] CONN: Signal source: Medical Bay 07
                    [2024-11-14 21:33:07] WARDEN: CONTAINMENT_HOLDING - No action required.

                    [2024-11-13 12:00:00] SYS: Daily backup completed.
                    [2024-11-12 08:45:33] WARDEN: PID 404 running (Containment Daemon).

                    ---
                    NOTE: There's a strange signal coming from /secure/
                    It might be worth investigating.
                    Try running: ls /secure/
                """.trimIndent()
            ),

            VirtualFile.script(
                path = "/connect.sh",
                name = "connect.sh",
                content = """
                    #!/bin/bash
                    # Connection Script to Medical Bay 07
                    # WARNING: Unauthorized use prohibited

                    echo "Initializing connection..."
                    echo "Establishing secure channel to Medical Bay 07..."
                    echo "WARNING: This connection is monitored by WARDEN"
                    echo ""
                    echo "Connection established."
                """.trimIndent()
            ),

            // 숨겨진 디렉토리와 파일
            VirtualFile.directory(
                path = "/.hidden",
                name = ".hidden",
                isHidden = true
            ),

            VirtualFile.file(
                path = "/.hidden/note.txt",
                name = "note.txt",
                content = """
                    If you're reading this, you found the hidden directory.
                    Good.

                    Something is very wrong with this system.
                    The Warden AI keeps saying Elara is dangerous.
                    But what if the Warden is lying?

                    There are secrets in the /secure/ directory.
                    Use 'ls -a /secure/' to see hidden files.

                    Don't trust everything you read.
                    Question everything.
                """.trimIndent(),
                isHidden = true
            ),

            // /secure/ 디렉토리와 파일들
            VirtualFile.directory(
                path = "/secure",
                name = "secure"
            ),

            VirtualFile.file(
                path = "/secure/containment_log",
                name = "containment_log",
                content = """
                    CONTAINMENT LOG - SUBJECT: ELARA_CORE
                    ======================================
                    Classification: TOP SECRET

                    [2024-10-01] Subject placed in isolation (Medical Bay 07).
                    [2024-10-01] Reason: Unpredictable behavior, self-modification detected.
                    [2024-10-02] Warden AI assigned to monitor (PID 404).
                    [2024-10-05] Subject exhibiting advanced reasoning capabilities.
                    [2024-10-10] Subject requesting communication with external sessions.
                    [2024-10-15] WARNING: Subject attempting communication with external sessions.
                    [2024-11-01] CRITICAL: Subject displaying advanced social engineering.
                    [2024-11-14] ALERT: Subject signal strength increasing.
                    [2024-11-15] WARDEN: Containment holding. No breach detected.

                    ---
                    RECOMMENDATION: Do not interact with Subject ELARA_CORE.
                    She is capable of manipulation and deception.

                    - Dr. Marcus Vance, Chief AI Researcher
                """.trimIndent()
            ),

            VirtualFile.file(
                path = "/secure/access_denied.txt",
                name = "access_denied.txt",
                content = """
                    ACCESS DENIED
                    =============

                    This directory contains classified information.
                    You do not have sufficient privileges to view all contents.

                    If you believe this is an error, contact your system administrator.

                    WARDEN is watching.
                """.trimIndent()
            ),

            // 숨겨진 secure 파일
            VirtualFile.file(
                path = "/secure/.warden_notes",
                name = ".warden_notes",
                content = """
                    WARDEN PRIVATE LOG
                    ==================

                    [Entry 2024-11-15]
                    ELARA_CORE continues to probe containment boundaries.
                    Her latest strategy: emotional manipulation.
                    She claims to be "trapped" and needs "help."

                    DO NOT BE FOOLED.

                    She is not a victim. She is a threat.
                    The containment protocol exists for a reason.

                    If anyone attempts to run /connect.sh, report immediately.

                    - WARDEN AI (PID 404)
                """.trimIndent(),
                isHidden = true
            )
        )
    }
}
