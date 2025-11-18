package com.rojojun.voidserver.adapter.`in`.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import java.time.Instant

/**
 * WebFlux Functional Router Configuration
 *
 * Functional Programming Style (DSL)
 * Route 정의를 함수형으로 선언
 */
@Configuration
class CommandHistoryRouter {

    /**
     * Functional Routes Bean
     *
     * Kotlin DSL: coRouter {} - Coroutine 기반 라우터
     */
    @Bean
    fun commandHistoryRoutes(handler: CommandHistoryHandler): RouterFunction<ServerResponse> =
        coRouter {
            GET("/health") {
                ServerResponse.ok().bodyValueAndAwait(
                    mapOf(
                        "status" to "UP",
                        "service" to "void-server",
                        "timestamp" to Instant.now()
                    )
                )
            }

            "/api/commands".nest {
                // Content Type: JSON
                accept(MediaType.APPLICATION_JSON).nest {

                    // POST /api/commands - 명령어 저장
                    POST("", handler::saveCommand)

                    // GET /api/commands/{id} - 단일 조회
                    GET("/{id}", handler::getCommandById)

                    // 세션 관련 엔드포인트
                    "/session/{sessionId}".nest {

                        // GET /api/commands/session/{sessionId} - 전체 히스토리
                        GET("", handler::getSessionHistory)

                        // GET /api/commands/session/{sessionId}/recent?limit=10
                        GET("/recent", handler::getRecentCommands)

                        // GET /api/commands/session/{sessionId}/search?keyword=ls
                        GET("/search", handler::searchCommands)

                        // GET /api/commands/session/{sessionId}/stats
                        GET("/stats", handler::getStats)
                    }
                }

                // Server-Sent Events (SSE) - 별도 Content Type
                accept(MediaType.TEXT_EVENT_STREAM).nest {
                    // GET /api/commands/session/{sessionId}/stream
                    GET("/session/{sessionId}/stream", handler::streamCommands)
                }
            }
        }
}