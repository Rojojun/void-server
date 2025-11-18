package com.rojojun.voidserver.application.usecase

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.port.`in`.CommandHistoryUseCase
import com.rojojun.voidserver.domain.port.out.CommandHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Application Service - 명령어 히스토리 유스케이스 구현
 *
 * Reactive Programming with Coroutine Flow
 * Multi-paradigm: Functional + OOP
 */
@Service
class CommandHistoryService(
    private val commandHistoryRepository: CommandHistoryRepository
) : CommandHistoryUseCase {

    /**
     * 명령어 저장 (suspend function)
     */
    override suspend fun saveCommand(command: Command): Command {
        return commandHistoryRepository.save(command)
            .also { saved ->
                // Side effect: 로깅 (함수형 프로그래밍의 also)
                println("✅ Command saved: ${saved.id} - ${saved.command}")
            }
    }

    /**
     * ID로 조회
     */
    override suspend fun getCommandById(id: Long): Command? {
        return commandHistoryRepository.findById(id)
    }

    /**
     * 세션 히스토리 조회 - Reactive Flow
     *
     * Flow: Cold stream (구독 시 실행)
     * List → Flow 변환
     */
    override fun getSessionHistory(sessionId: UUID): Flow<Command> {
        return kotlinx.coroutines.flow.flow {
            val commands = commandHistoryRepository.findBySessionId(sessionId)
            commands.forEach { emit(it) }  // Emit each command
        }
    }

    /**
     * 최근 명령어 조회 - Reactive Flow
     *
     * Functional style: limit 파라미터로 제어
     */
    override fun getRecentCommands(sessionId: UUID, limit: Int): Flow<Command> {
        return kotlinx.coroutines.flow.flow {
            val commands = commandHistoryRepository.findRecentBySessionId(sessionId, limit)
            commands.forEach { emit(it) }
        }
    }

    /**
     * 명령어 개수
     */
    override suspend fun getCommandCount(sessionId: UUID): Long {
        return commandHistoryRepository.countBySessionId(sessionId)
    }

    /**
     * 명령어 검색 - Functional + Reactive
     *
     * High-order function: filter (함수형 프로그래밍)
     * Flow transformation pipeline
     */
    override fun searchCommands(sessionId: UUID, keyword: String): Flow<Command> {
        return kotlinx.coroutines.flow.flow {
            commandHistoryRepository.findBySessionId(sessionId)
                .asFlow()
                .filter { command ->
                    // Functional predicate: 대소문자 구분 없이 검색
                    command.command.contains(keyword, ignoreCase = true) ||
                    command.response.contains(keyword, ignoreCase = true)
                }
                .collect { emit(it) }
        }
    }
}

/**
 * Extension Function (Kotlin Functional Programming)
 *
 * Command에 통계 정보 추가
 */
fun Command.withStats(totalCount: Long, rank: Int): Map<String, Any> {
    return mapOf(
        "command" to this,
        "stats" to mapOf(
            "totalCommands" to totalCount,
            "rank" to rank
        )
    )
}

/**
 * Functional Composition Example
 *
 * 명령어 리스트를 통계와 함께 변환
 */
fun List<Command>.enrichWithStats(): List<Map<String, Any>> {
    return this.mapIndexed { index, command ->
        command.withStats(
            totalCount = this.size.toLong(),
            rank = index + 1
        )
    }
}
