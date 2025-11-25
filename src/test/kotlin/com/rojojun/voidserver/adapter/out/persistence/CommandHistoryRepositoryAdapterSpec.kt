package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.adapter.out.persistence.entity.CommandEntity
import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import java.util.UUID

/**
 * Adapter 단위 테스트 - CommandHistoryRepositoryAdapter (Kotest BehaviorSpec)
 *
 * BehaviorSpec 사용 이유:
 * - Given-When-Then 명확히 표현
 * - 비즈니스 행동(Behavior) 검증
 * - MockK와 조합하여 단위 테스트
 */
class CommandHistoryRepositoryAdapterSpec : BehaviorSpec({

    val r2dbcRepository = mockk<R2dbcCommandRepository>()
    val adapter = CommandHistoryRepositoryAdapter(r2dbcRepository)

    Given("도메인 Command가 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val timestamp = Instant.now()

        val domainCommand = Command(
            sessionId = sessionId,
            command = "ls -la",
            response = "file1.txt",
            intent = CommandIntent.LIST_FILES,
            timestamp = timestamp
        )

        val savedEntity = CommandEntity(
            id = 1L,
            sessionId = sessionId,
            command = "ls -la",
            response = "file1.txt",
            intent = CommandIntent.LIST_FILES,
            timestamp = timestamp
        )

        When("save를 호출하면") {
            coEvery { r2dbcRepository.save(any()) } returns savedEntity

            val result = adapter.save(domainCommand)

            Then("엔티티로 변환되어 저장되고 도메인 모델로 반환된다") {
                result.id.shouldNotBeNull()
                result.id shouldBe 1L
                result.sessionId shouldBe sessionId
                result.command shouldBe "ls -la"
                result.intent shouldBe CommandIntent.LIST_FILES

                coVerify(exactly = 1) { r2dbcRepository.save(any()) }
            }
        }
    }

    Given("intent가 null인 도메인 Command가 주어졌을 때") {
        val sessionId = UUID.randomUUID()

        val domainCommand = Command(
            sessionId = sessionId,
            command = "unknown command",
            response = "unknown",
            intent = null
        )

        val savedEntity = CommandEntity(
            id = 2L,
            sessionId = sessionId,
            command = "unknown command",
            response = "unknown",
            intent = null,
            timestamp = Instant.now()
        )

        When("save를 호출하면") {
            coEvery { r2dbcRepository.save(any()) } returns savedEntity

            val result = adapter.save(domainCommand)

            Then("intent가 null인 상태로 저장된다") {
                result.id shouldBe 2L
                result.intent.shouldBeNull()

                coVerify(exactly = 1) { r2dbcRepository.save(any()) }
            }
        }
    }

    Given("저장된 엔티티 ID가 주어졌을 때") {
        val id = 10L
        val sessionId = UUID.randomUUID()
        val timestamp = Instant.now()

        val entity = CommandEntity(
            id = id,
            sessionId = sessionId,
            command = "help",
            response = "Available commands",
            intent = CommandIntent.HELP,
            timestamp = timestamp
        )

        When("findById를 호출하면") {
            coEvery { r2dbcRepository.findById(id) } returns entity

            val result = adapter.findById(id)

            Then("엔티티가 도메인 모델로 변환되어 반환된다") {
                result.shouldNotBeNull()
                result.id shouldBe id
                result.sessionId shouldBe sessionId
                result.command shouldBe "help"
                result.intent shouldBe CommandIntent.HELP

                coVerify(exactly = 1) { r2dbcRepository.findById(id) }
            }
        }
    }

    Given("존재하지 않는 ID가 주어졌을 때") {
        val id = 999L

        When("findById를 호출하면") {
            coEvery { r2dbcRepository.findById(id) } returns null

            val result = adapter.findById(id)

            Then("null이 반환된다") {
                result.shouldBeNull()

                coVerify(exactly = 1) { r2dbcRepository.findById(id) }
            }
        }
    }

    Given("세션 ID가 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val now = Instant.now()

        val entities = listOf(
            CommandEntity(
                id = 1L,
                sessionId = sessionId,
                command = "first",
                response = "res1",
                timestamp = now.minusSeconds(10)
            ),
            CommandEntity(
                id = 2L,
                sessionId = sessionId,
                command = "second",
                response = "res2",
                timestamp = now
            )
        )

        When("findBySessionId를 호출하면") {
            coEvery {
                r2dbcRepository.findBySessionIdOrderByTimestampDesc(sessionId)
            } returns entities

            val result = adapter.findBySessionId(sessionId)

            Then("세션의 모든 명령어가 도메인 모델로 변환되어 반환된다") {
                result shouldHaveSize 2
                result[0].command shouldBe "first"
                result[1].command shouldBe "second"

                coVerify(exactly = 1) {
                    r2dbcRepository.findBySessionIdOrderByTimestampDesc(sessionId)
                }
            }
        }
    }

    Given("빈 세션 ID가 주어졌을 때") {
        val sessionId = UUID.randomUUID()

        When("findBySessionId를 호출하면") {
            coEvery {
                r2dbcRepository.findBySessionIdOrderByTimestampDesc(sessionId)
            } returns emptyList()

            val result = adapter.findBySessionId(sessionId)

            Then("빈 리스트가 반환된다") {
                result shouldHaveSize 0
            }
        }
    }

    Given("세션 ID와 limit이 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val now = Instant.now()

        val entities = (0..9).map { i ->
            CommandEntity(
                id = i.toLong(),
                sessionId = sessionId,
                command = "cmd-$i",
                response = "res-$i",
                timestamp = now.minusSeconds(i.toLong())
            )
        }

        When("findRecentBySessionId를 호출하면") {
            coEvery {
                r2dbcRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId)
            } returns entities

            val result = adapter.findRecentBySessionId(sessionId, limit = 5)

            Then("최근 N개의 명령어만 반환된다") {
                result shouldHaveSize 5
                result[0].command shouldBe "cmd-0"
                result[4].command shouldBe "cmd-4"

                coVerify(exactly = 1) {
                    r2dbcRepository.findTop10BySessionIdOrderByTimestampDesc(sessionId)
                }
            }
        }
    }

    Given("세션 ID가 주어졌을 때") {
        val sessionId = UUID.randomUUID()

        When("countBySessionId를 호출하면") {
            coEvery { r2dbcRepository.countBySessionId(sessionId) } returns 42L

            val result = adapter.countBySessionId(sessionId)

            Then("명령어 개수가 반환된다") {
                result shouldBe 42L

                coVerify(exactly = 1) { r2dbcRepository.countBySessionId(sessionId) }
            }
        }
    }

    Given("모든 CommandIntent가 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val intents = CommandIntent.entries

        When("각 Intent로 save를 호출하면") {
            intents.forEach { intent ->
                val domainCommand = Command(
                    sessionId = sessionId,
                    command = "test",
                    response = "test",
                    intent = intent
                )

                val savedEntity = CommandEntity.from(domainCommand).copy(id = 1L)

                coEvery { r2dbcRepository.save(any()) } returns savedEntity

                val result = adapter.save(domainCommand)

                Then("모든 Intent가 정상적으로 변환된다 - $intent") {
                    result.intent shouldBe intent
                }
            }
        }
    }
})
