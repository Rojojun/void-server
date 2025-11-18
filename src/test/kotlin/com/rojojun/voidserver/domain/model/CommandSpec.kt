package com.rojojun.voidserver.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.UUID

/**
 * 도메인 모델 테스트 - Command (Kotest DescribeSpec)
 *
 * DescribeSpec 사용 이유:
 * - BDD 스타일 (describe-context-it)
 * - 계층적 구조로 테스트 그룹화
 * - 도메인 모델의 다양한 동작 명확히 표현
 */
class CommandSpec : DescribeSpec({

    describe("Command 생성") {
        context("필수 필드만 제공하면") {
            it("기본값으로 Command가 생성된다") {
                // Given
                val sessionId = UUID.randomUUID()
                val commandText = "ls -la"
                val response = "file1.txt\nfile2.txt"

                // When
                val command = Command(
                    sessionId = sessionId,
                    command = commandText,
                    response = response
                )

                // Then
                command.id.shouldBeNull()
                command.sessionId shouldBe sessionId
                command.command shouldBe commandText
                command.response shouldBe response
                command.intent.shouldBeNull()
                command.timestamp.shouldNotBeNull()
            }
        }

        context("모든 필드를 제공하면") {
            it("지정한 값으로 Command가 생성된다") {
                // Given
                val id = 1L
                val sessionId = UUID.randomUUID()
                val commandText = "kill 404"
                val response = "Process terminated"
                val intent = CommandIntent.KILL_PROCESS
                val timestamp = Instant.now()

                // When
                val command = Command(
                    id = id,
                    sessionId = sessionId,
                    command = commandText,
                    response = response,
                    intent = intent,
                    timestamp = timestamp
                )

                // Then
                command.id shouldBe id
                command.sessionId shouldBe sessionId
                command.command shouldBe commandText
                command.response shouldBe response
                command.intent shouldBe intent
                command.timestamp shouldBe timestamp
            }
        }
    }

    describe("Command data class 특성") {
        context("copy 메서드를 사용하면") {
            it("일부 필드만 변경된 새 인스턴스가 생성된다") {
                // Given
                val original = Command(
                    sessionId = UUID.randomUUID(),
                    command = "help",
                    response = "Available commands..."
                )

                // When
                val copied = original.copy(
                    id = 100L,
                    intent = CommandIntent.HELP
                )

                // Then
                copied.id shouldBe 100L
                copied.intent shouldBe CommandIntent.HELP
                copied.sessionId shouldBe original.sessionId
                copied.command shouldBe original.command
                copied.response shouldBe original.response
            }
        }

        context("동일한 값으로 생성된 두 인스턴스는") {
            it("equals로 동등하다고 판단된다") {
                // Given
                val sessionId = UUID.randomUUID()
                val timestamp = Instant.now()

                val command1 = Command(
                    id = 1L,
                    sessionId = sessionId,
                    command = "ls",
                    response = "files",
                    intent = CommandIntent.LIST_FILES,
                    timestamp = timestamp
                )

                val command2 = Command(
                    id = 1L,
                    sessionId = sessionId,
                    command = "ls",
                    response = "files",
                    intent = CommandIntent.LIST_FILES,
                    timestamp = timestamp
                )

                // Then
                command1 shouldBe command2
            }
        }
    }

    describe("CommandIntent enum") {
        context("모든 Intent 값을 조회하면") {
            it("7개의 Intent가 존재한다") {
                // When
                val intents = CommandIntent.entries

                // Then
                intents shouldHaveSize 7
                intents shouldContain CommandIntent.LIST_FILES
                intents shouldContain CommandIntent.READ_FILE
                intents shouldContain CommandIntent.EXECUTE_SCRIPT
                intents shouldContain CommandIntent.KILL_PROCESS
                intents shouldContain CommandIntent.HELP
                intents shouldContain CommandIntent.ABORT
                intents shouldContain CommandIntent.UNKNOWN
            }
        }

        context("유효한 문자열로 valueOf를 호출하면") {
            it("해당 Intent enum이 반환된다") {
                // When
                val intent = CommandIntent.valueOf("KILL_PROCESS")

                // Then
                intent shouldBe CommandIntent.KILL_PROCESS
            }
        }

        context("잘못된 문자열로 valueOf를 호출하면") {
            it("IllegalArgumentException이 발생한다") {
                // When & Then
                shouldThrow<IllegalArgumentException> {
                    CommandIntent.valueOf("INVALID_INTENT")
                }
            }
        }
    }
})
