package com.rojojun.voidserver.adapter.`in`.web

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.port.out.CommandHistoryRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import java.util.UUID

/**
 * 통합 테스트 - Command History API
 *
 * 전체 흐름 테스트: Router → Handler → UseCase → Repository → Database
 * WebTestClient를 사용한 실제 HTTP 요청 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CommandHistoryIntegrationTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var repository: CommandHistoryRepository

    init {
        Given("명령어 저장 요청이 주어졌을 때") {
            val sessionId = UUID.randomUUID()
            val request = SaveCommandRequest(
                sessionId = sessionId.toString(),
                command = "ls -la",
                response = "file1.txt\nfile2.txt\nfile3.txt",
                intent = "LIST_FILES"
            )

            When("POST /api/commands로 저장하면") {
                val response = webTestClient
                    .post()
                    .uri("/api/commands")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<CommandResponse>()
                    .returnResult()
                    .responseBody!!

                Then("저장된 명령어가 반환되고") {
                    response.id.shouldNotBeNull()
                    response.sessionId shouldBe sessionId.toString()
                    response.command shouldBe "ls -la"
                    response.intent shouldBe "LIST_FILES"
                }

                And("DB에 실제로 저장된다") {
                    val saved = repository.findById(response.id!!)
                    saved.shouldNotBeNull()
                    saved.command shouldBe "ls -la"
                    saved.intent shouldBe CommandIntent.LIST_FILES
                }
            }
        }

        Given("저장된 명령어가 있을 때") {
            val sessionId = UUID.randomUUID()
            val command = Command(
                sessionId = sessionId,
                command = "cat file.txt",
                response = "Hello World",
                intent = CommandIntent.READ_FILE
            )
            val saved = repository.save(command)

            When("GET /api/commands/{id}로 조회하면") {
                val response = webTestClient
                    .get()
                    .uri("/api/commands/${saved.id}")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<CommandResponse>()
                    .returnResult()
                    .responseBody!!

                Then("저장된 명령어가 반환된다") {
                    response.id shouldBe saved.id
                    response.command shouldBe "cat file.txt"
                    response.response shouldBe "Hello World"
                    response.intent shouldBe "READ_FILE"
                }
            }

            When("존재하지 않는 ID로 조회하면") {
                webTestClient
                    .get()
                    .uri("/api/commands/99999")
                    .exchange()
                    .expectStatus().isNotFound

                Then("404 Not Found가 반환된다") {
                    // 상태 코드 검증 완료
                }
            }
        }

        Given("세션에 여러 명령어가 저장되어 있을 때") {
            val sessionId = UUID.randomUUID()
            val commands = listOf(
                Command(
                    sessionId = sessionId,
                    command = "ls",
                    response = "files",
                    intent = CommandIntent.LIST_FILES
                ),
                Command(
                    sessionId = sessionId,
                    command = "cat file.txt",
                    response = "content",
                    intent = CommandIntent.READ_FILE
                ),
                Command(
                    sessionId = sessionId,
                    command = "help",
                    response = "Available commands",
                    intent = CommandIntent.HELP
                )
            )

            commands.forEach { repository.save(it) }

            When("GET /api/commands/session/{sessionId}로 조회하면") {
                val response = webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<CommandResponse>()
                    .returnResult()
                    .responseBody!!

                Then("모든 명령어가 반환된다") {
                    response shouldHaveSize 3
                    response.map { it.command } shouldBe listOf("ls", "cat file.txt", "help")
                }
            }
        }

        Given("세션에 10개의 명령어가 있을 때") {
            val sessionId = UUID.randomUUID()
            val commands = (0 until 10).map { i ->
                Command(
                    sessionId = sessionId,
                    command = "cmd-$i",
                    response = "res-$i"
                )
            }

            commands.forEach { repository.save(it) }

            When("GET /api/commands/session/{sessionId}/recent?limit=5로 조회하면") {
                val response = webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId/recent?limit=5")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<CommandResponse>()
                    .returnResult()
                    .responseBody!!

                Then("최근 5개만 반환된다") {
                    response shouldHaveSize 5
                }
            }

            When("limit 없이 조회하면") {
                val response = webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId/recent")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<CommandResponse>()
                    .returnResult()
                    .responseBody!!

                Then("기본값 10개가 반환된다") {
                    response shouldHaveSize 10
                }
            }
        }

        Given("다양한 명령어가 저장되어 있을 때") {
            val sessionId = UUID.randomUUID()
            val commands = listOf(
                Command(sessionId = sessionId, command = "ls -la", response = "files"),
                Command(sessionId = sessionId, command = "cat file.txt", response = "content"),
                Command(sessionId = sessionId, command = "grep 'pattern'", response = "matches")
            )

            commands.forEach { repository.save(it) }

            When("keyword='ls'로 검색하면") {
                val response = webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId/search?keyword=ls")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<CommandResponse>()
                    .returnResult()
                    .responseBody!!

                Then("'ls'가 포함된 명령어만 반환된다") {
                    response shouldHaveSize 1
                    response[0].command shouldBe "ls -la"
                }
            }

            When("keyword='file'로 검색하면") {
                val response = webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId/search?keyword=file")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList<CommandResponse>()
                    .returnResult()
                    .responseBody!!

                Then("'file'이 포함된 명령어들이 반환된다") {
                    response shouldHaveSize 2
                    response.map { it.command }.toSet() shouldBe setOf("cat file.txt", "ls -la")
                }
            }

            When("keyword 없이 검색하면") {
                webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId/search")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest

                Then("400 Bad Request가 반환된다") {
                    // 상태 코드 검증 완료
                }
            }
        }

        Given("세션에 명령어들이 있을 때 통계를 요청하면") {
            val sessionId = UUID.randomUUID()
            val commands = (0 until 15).map { i ->
                Command(sessionId = sessionId, command = "cmd-$i", response = "res-$i")
            }

            commands.forEach { repository.save(it) }

            When("GET /api/commands/session/{sessionId}/stats로 조회하면") {
                val response = webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId/stats")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
                    .expectBody<SessionStatsResponse>()
                    .returnResult()
                    .responseBody!!

                Then("전체 개수와 최근 5개가 반환된다") {
                    response.totalCommands shouldBe 15L
                    response.recentCommands shouldHaveSize 5
                    response.sessionId shouldBe sessionId
                }
            }
        }

        Given("전체 API 흐름 테스트") {
            val sessionId = UUID.randomUUID()

            When("명령어를 3개 저장하고") {
                val requests = listOf(
                    SaveCommandRequest(sessionId.toString(), "ls", "files", "LIST_FILES"),
                    SaveCommandRequest(sessionId.toString(), "cat file.txt", "content", "READ_FILE"),
                    SaveCommandRequest(sessionId.toString(), "help", "commands", "HELP")
                )

                val savedIds = requests.map { request ->
                    webTestClient
                        .post()
                        .uri("/api/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<CommandResponse>()
                        .returnResult()
                        .responseBody!!
                        .id!!
                }

                Then("세션 히스토리에 3개가 조회되고") {
                    val history = webTestClient
                        .get()
                        .uri("/api/commands/session/$sessionId")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk
                        .expectBodyList<CommandResponse>()
                        .returnResult()
                        .responseBody!!

                    history shouldHaveSize 3
                }
                And("각 명령어를 ID로 조회할 수 있고") {
                    savedIds.forEach { id ->
                        webTestClient
                            .get()
                            .uri("/api/commands/$id")
                            .exchange()
                            .expectStatus().isOk
                    }

                    And("통계를 조회할 수 있다") {
                        val stats = webTestClient
                            .get()
                            .uri("/api/commands/session/$sessionId/stats")
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isOk
                            .expectBody<SessionStatsResponse>()
                            .returnResult()
                            .responseBody!!

                        stats.totalCommands shouldBe 3L
                        stats.recentCommands shouldHaveSize 3
                    }
                }
            }
        }

        Given("SSE 스트림 테스트") {
            val sessionId = UUID.randomUUID()
            val commands = listOf(
                Command(sessionId = sessionId, command = "cmd1", response = "res1"),
                Command(sessionId = sessionId, command = "cmd2", response = "res2")
            )

            commands.forEach { repository.save(it) }

            When("GET /api/commands/session/{sessionId}/stream으로 요청하면") {
                // SSE는 text/event-stream으로 응답
                val response = webTestClient
                    .get()
                    .uri("/api/commands/session/$sessionId/stream")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk

                Then("SSE 스트림이 반환된다") {
                    // SSE는 스트리밍이므로 상태 코드만 검증
                    // 실제 스트림 내용은 별도 테스트 필요
                }
            }
        }
    }
}
