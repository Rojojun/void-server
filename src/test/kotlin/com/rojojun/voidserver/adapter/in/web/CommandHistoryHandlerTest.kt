package com.rojojun.voidserver.adapter.`in`.web

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.port.`in`.CommandHistoryUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import java.time.Instant
import java.util.UUID

/**
 * Handler 단위 테스트 - CommandHistoryHandler
 *
 * UseCase를 Mock으로 대체하여 Handler 로직만 테스트
 * BehaviorSpec: Given-When-Then 패턴
 */
class CommandHistoryHandlerTest : BehaviorSpec({

    val useCase = mockk<CommandHistoryUseCase>()
    val handler = CommandHistoryHandler(useCase)

    Given("명령어 저장 요청이 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val request = mockk<ServerRequest>()
        val dto = SaveCommandRequest(
            sessionId = sessionId.toString(),
            command = "ls -la",
            response = "file1.txt\nfile2.txt",
            intent = "LIST_FILES"
        )

        val savedCommand = Command(
            id = 1L,
            sessionId = sessionId,
            command = "ls -la",
            response = "file1.txt\nfile2.txt",
            intent = CommandIntent.LIST_FILES,
            timestamp = Instant.now()
        )

        coEvery { request.awaitBody<SaveCommandRequest>() } returns dto
        coEvery { useCase.saveCommand(any()) } returns savedCommand

        When("saveCommand를 호출하면") {
            val response = handler.saveCommand(request)

            Then("200 OK 응답이 반환된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.saveCommand(any()) }
            }
        }
    }

    Given("존재하는 명령어 ID가 주어졌을 때") {
        val commandId = 10L
        val request = mockk<ServerRequest>()
        val command = Command(
            id = commandId,
            sessionId = UUID.randomUUID(),
            command = "cat file.txt",
            response = "Hello World",
            intent = CommandIntent.READ_FILE,
            timestamp = Instant.now()
        )

        every { request.pathVariable("id") } returns commandId.toString()
        coEvery { useCase.getCommandById(commandId) } returns command

        When("getCommandById를 호출하면") {
            val response = handler.getCommandById(request)

            Then("200 OK와 명령어 데이터가 반환된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.getCommandById(commandId) }
            }
        }
    }

    Given("존재하지 않는 명령어 ID가 주어졌을 때") {
        val commandId = 999L
        val request = mockk<ServerRequest>()

        every { request.pathVariable("id") } returns commandId.toString()
        coEvery { useCase.getCommandById(commandId) } returns null

        When("getCommandById를 호출하면") {
            val response = handler.getCommandById(request)

            Then("404 Not Found가 반환된다") {
                response.statusCode() shouldBe HttpStatus.NOT_FOUND
            }
        }
    }

    Given("세션 ID가 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val request = mockk<ServerRequest>()

        val commands = listOf(
            Command(
                id = 1L,
                sessionId = sessionId,
                command = "ls",
                response = "files",
                intent = CommandIntent.LIST_FILES
            ),
            Command(
                id = 2L,
                sessionId = sessionId,
                command = "cat file.txt",
                response = "content",
                intent = CommandIntent.READ_FILE
            )
        )

        every { request.pathVariable("sessionId") } returns sessionId.toString()
        coEvery { useCase.getSessionHistory(sessionId) } returns flowOf(*commands.toTypedArray())

        When("getSessionHistory를 호출하면") {
            val response = handler.getSessionHistory(request)

            Then("200 OK와 명령어 목록이 반환된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.getSessionHistory(sessionId) }
            }
        }
    }

    Given("세션 ID와 limit이 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val request = mockk<ServerRequest>()
        val limit = 5

        val recentCommands = (0 until limit).map { i ->
            Command(
                id = i.toLong(),
                sessionId = sessionId,
                command = "cmd-$i",
                response = "res-$i"
            )
        }

        every { request.pathVariable("sessionId") } returns sessionId.toString()
        every { request.queryParamOrNull("limit") } returns limit.toString()
        coEvery { useCase.getRecentCommands(sessionId, limit) } returns flowOf(*recentCommands.toTypedArray())

        When("getRecentCommands를 호출하면") {
            val response = handler.getRecentCommands(request)

            Then("200 OK와 최근 명령어들이 반환된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.getRecentCommands(sessionId, limit) }
            }
        }
    }

    Given("limit이 없을 때") {
        val sessionId = UUID.randomUUID()
        val request = mockk<ServerRequest>()
        val defaultLimit = 10

        every { request.pathVariable("sessionId") } returns sessionId.toString()
        every { request.queryParamOrNull("limit") } returns null
        coEvery { useCase.getRecentCommands(sessionId, defaultLimit) } returns flowOf()

        When("getRecentCommands를 호출하면") {
            val response = handler.getRecentCommands(request)

            Then("기본값 10으로 호출된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.getRecentCommands(sessionId, defaultLimit) }
            }
        }
    }

    Given("검색 키워드가 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val keyword = "ls"
        val request = mockk<ServerRequest>()

        val searchResults = listOf(
            Command(
                id = 1L,
                sessionId = sessionId,
                command = "ls -la",
                response = "files",
                intent = CommandIntent.LIST_FILES
            )
        )

        every { request.pathVariable("sessionId") } returns sessionId.toString()
        every { request.queryParamOrNull("keyword") } returns keyword
        coEvery { useCase.searchCommands(sessionId, keyword) } returns flowOf(*searchResults.toTypedArray())

        When("searchCommands를 호출하면") {
            val response = handler.searchCommands(request)

            Then("200 OK와 검색 결과가 반환된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.searchCommands(sessionId, keyword) }
            }
        }
    }

    Given("검색 키워드가 없을 때") {
        val sessionId = UUID.randomUUID()
        val request = mockk<ServerRequest>()

        every { request.pathVariable("sessionId") } returns sessionId.toString()
        every { request.queryParamOrNull("keyword") } returns null

        When("searchCommands를 호출하면") {
            val response = handler.searchCommands(request)

            Then("400 Bad Request가 반환된다") {
                response.statusCode() shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }

    Given("세션 ID가 주어졌을 때 통계를 요청하면") {
        val sessionId = UUID.randomUUID()
        val request = mockk<ServerRequest>()
        val totalCount = 42L
        val recentCommands = listOf(
            Command(id = 1L, sessionId = sessionId, command = "ls", response = "files"),
            Command(id = 2L, sessionId = sessionId, command = "cat", response = "content")
        )

        every { request.pathVariable("sessionId") } returns sessionId.toString()
        coEvery { useCase.getCommandCount(sessionId) } returns totalCount
        coEvery { useCase.getRecentCommands(sessionId, 5) } returns flowOf(*recentCommands.toTypedArray())

        When("getStats를 호출하면") {
            val response = handler.getStats(request)

            Then("200 OK와 통계 정보가 반환된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.getCommandCount(sessionId) }
                coVerify(exactly = 1) { useCase.getRecentCommands(sessionId, 5) }
            }
        }
    }

    Given("SSE 스트림 요청이 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val request = mockk<ServerRequest>()
        val commands = listOf(
            Command(id = 1L, sessionId = sessionId, command = "cmd1", response = "res1"),
            Command(id = 2L, sessionId = sessionId, command = "cmd2", response = "res2")
        )

        every { request.pathVariable("sessionId") } returns sessionId.toString()
        coEvery { useCase.getSessionHistory(sessionId) } returns flowOf(*commands.toTypedArray())

        When("streamCommands를 호출하면") {
            val response = handler.streamCommands(request)

            Then("200 OK와 SSE 스트림이 반환된다") {
                response.statusCode() shouldBe HttpStatus.OK

                coVerify(exactly = 1) { useCase.getSessionHistory(sessionId) }
            }
        }
    }
})