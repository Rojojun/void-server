package com.rojojun.voidserver.adapter.`in`.web

import com.rojojun.voidserver.domain.port.`in`.CommandHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.util.UUID

/**
 * WebFlux Functional Handler
 *
 * Reactive + Functional Programming
 * Coroutine DSL for Reactive Streams
 */
@Component
class CommandHistoryHandler(
    private val commandHistoryUseCase: CommandHistoryUseCase
) {

    /**
     * 명령어 저장 - POST /api/commands
     *
     * Reactive Pattern: Mono 처리
     */
    suspend fun saveCommand(request: ServerRequest): ServerResponse {
        val dto = request.awaitBody<SaveCommandRequest>()

        // Functional transformation
        val command = dto.toDomain()

        val saved = commandHistoryUseCase.saveCommand(command)

        // ServerResponse with Mono
        return ServerResponse
            .ok()
            .bodyValueAndAwait(CommandResponse.from(saved))
    }

    /**
     * ID로 조회 - GET /api/commands/{id}
     *
     * Nullable handling with Functional style
     */
    suspend fun getCommandById(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLong()

        return commandHistoryUseCase.getCommandById(id)
            ?.let { command ->
                ServerResponse.ok().bodyValueAndAwait(CommandResponse.from(command))
            }
            ?: ServerResponse.notFound().buildAndAwait()
    }

    /**
     * 세션 히스토리 조회 - GET /api/commands/session/{sessionId}
     *
     * Reactive Stream: Flow → Server-Sent Events (SSE)
     */
    suspend fun getSessionHistory(request: ServerRequest): ServerResponse {
        val sessionId = UUID.fromString(request.pathVariable("sessionId"))

        val commandsFlow: Flow<CommandResponse> = commandHistoryUseCase
            .getSessionHistory(sessionId)
            .map { CommandResponse.from(it) }

        // Stream as JSON array
        return ServerResponse
            .ok()
            .bodyAndAwait(commandsFlow)
    }

    /**
     * 최근 명령어 - GET /api/commands/session/{sessionId}/recent?limit=10
     *
     * Query parameter handling
     */
    suspend fun getRecentCommands(request: ServerRequest): ServerResponse {
        val sessionId = UUID.fromString(request.pathVariable("sessionId"))
        val limit = request.queryParamOrNull("limit")?.toInt() ?: 10

        val commandsFlow = commandHistoryUseCase
            .getRecentCommands(sessionId, limit)
            .map { CommandResponse.from(it) }

        return ServerResponse
            .ok()
            .bodyAndAwait(commandsFlow)
    }

    /**
     * 검색 - GET /api/commands/session/{sessionId}/search?keyword=ls
     *
     * Reactive search with filter
     */
    suspend fun searchCommands(request: ServerRequest): ServerResponse {
        val sessionId = UUID.fromString(request.pathVariable("sessionId"))
        val keyword = request.queryParamOrNull("keyword")
            ?: return ServerResponse.badRequest().buildAndAwait()

        val results = commandHistoryUseCase
            .searchCommands(sessionId, keyword)
            .map { CommandResponse.from(it) }

        return ServerResponse
            .ok()
            .bodyAndAwait(results)
    }

    /**
     * 통계 - GET /api/commands/session/{sessionId}/stats
     *
     * Aggregation example
     */
    suspend fun getStats(request: ServerRequest): ServerResponse {
        val sessionId = UUID.fromString(request.pathVariable("sessionId"))

        val count = commandHistoryUseCase.getCommandCount(sessionId)
        val recentCommands = commandHistoryUseCase
            .getRecentCommands(sessionId, 5)
            .toList()

        val stats = SessionStatsResponse(
            sessionId = sessionId,
            totalCommands = count,
            recentCommands = recentCommands.map { CommandResponse.from(it) }
        )

        return ServerResponse
            .ok()
            .bodyValueAndAwait(stats)
    }

    /**
     * Server-Sent Events Stream - GET /api/commands/session/{sessionId}/stream
     *
     * Real-time streaming with SSE
     */
    suspend fun streamCommands(request: ServerRequest): ServerResponse {
        val sessionId = UUID.fromString(request.pathVariable("sessionId"))

        val commandsFlow = commandHistoryUseCase
            .getSessionHistory(sessionId)
            .map { CommandResponse.from(it) }

        return ServerResponse
            .ok()
            .sse()
            .bodyAndAwait(commandsFlow)
    }
}
