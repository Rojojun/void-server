package com.rojojun.voidserver.adapter.out.persistence.entity

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.UUID

/**
 * Entity 변환 테스트 - CommandEntity (Kotest FunSpec)
 *
 * FunSpec 사용 이유:
 * - 단순한 함수형 테스트에 적합
 * - Entity ↔ Domain 변환 로직 검증
 * - 간결하고 직관적
 */
class CommandEntitySpec : FunSpec({

    test("CommandEntity 생성 - 기본값 포함") {
        // Given
        val sessionId = UUID.randomUUID()
        val command = "ls -la"
        val response = "file1.txt"

        // When
        val entity = CommandEntity(
            sessionId = sessionId,
            command = command,
            response = response
        )

        // Then
        entity.id.shouldBeNull()
        entity.sessionId shouldBe sessionId
        entity.command shouldBe command
        entity.response shouldBe response
        entity.intent.shouldBeNull()
        entity.timestamp.shouldNotBeNull()
    }

    test("CommandEntity to Domain 변환 - intent null") {
        // Given
        val sessionId = UUID.randomUUID()
        val timestamp = Instant.now()

        val entity = CommandEntity(
            id = 1L,
            sessionId = sessionId,
            command = "help",
            response = "Available commands",
            intent = null,
            timestamp = timestamp
        )

        // When
        val domain = entity.toDomain()

        // Then
        domain.id shouldBe 1L
        domain.sessionId shouldBe sessionId
        domain.command shouldBe "help"
        domain.response shouldBe "Available commands"
        domain.intent.shouldBeNull()
        domain.timestamp shouldBe timestamp
    }

    test("CommandEntity to Domain 변환 - intent 포함") {
        // Given
        val sessionId = UUID.randomUUID()
        val timestamp = Instant.now()

        val entity = CommandEntity(
            id = 2L,
            sessionId = sessionId,
            command = "kill 404",
            response = "Process terminated",
            intent = "KILL_PROCESS",
            timestamp = timestamp
        )

        // When
        val domain = entity.toDomain()

        // Then
        domain.id shouldBe 2L
        domain.intent shouldBe CommandIntent.KILL_PROCESS
    }

    test("Domain to CommandEntity 변환 - intent null") {
        // Given
        val sessionId = UUID.randomUUID()
        val timestamp = Instant.now()

        val domain = Command(
            id = 3L,
            sessionId = sessionId,
            command = "cat file.txt",
            response = "file contents",
            intent = null,
            timestamp = timestamp
        )

        // When
        val entity = CommandEntity.from(domain)

        // Then
        entity.id shouldBe 3L
        entity.sessionId shouldBe sessionId
        entity.command shouldBe "cat file.txt"
        entity.response shouldBe "file contents"
        entity.intent.shouldBeNull()
        entity.timestamp shouldBe timestamp
    }

    test("Domain to CommandEntity 변환 - intent 포함") {
        // Given
        val sessionId = UUID.randomUUID()
        val timestamp = Instant.now()

        val domain = Command(
            id = 4L,
            sessionId = sessionId,
            command = "ls",
            response = "file1 file2",
            intent = CommandIntent.LIST_FILES,
            timestamp = timestamp
        )

        // When
        val entity = CommandEntity.from(domain)

        // Then
        entity.id shouldBe 4L
        entity.intent shouldBe "LIST_FILES"
    }

    test("Entity ↔ Domain 양방향 변환 일관성") {
        // Given
        val original = Command(
            id = 5L,
            sessionId = UUID.randomUUID(),
            command = "run script.sh",
            response = "Script executed",
            intent = CommandIntent.EXECUTE_SCRIPT,
            timestamp = Instant.now()
        )

        // When
        val entity = CommandEntity.from(original)
        val converted = entity.toDomain()

        // Then
        converted shouldBe original
    }

    test("모든 CommandIntent enum을 문자열로 변환 가능") {
        // Given
        val sessionId = UUID.randomUUID()
        val intents = CommandIntent.entries

        // When & Then
        intents.forEach { intent ->
            val domain = Command(
                sessionId = sessionId,
                command = "test",
                response = "test",
                intent = intent
            )

            val entity = CommandEntity.from(domain)
            val converted = entity.toDomain()

            converted.intent shouldBe intent
        }
    }

    test("Entity copy - immutable 확인") {
        // Given
        val original = CommandEntity(
            id = 10L,
            sessionId = UUID.randomUUID(),
            command = "test",
            response = "test response",
            intent = "HELP",
            timestamp = Instant.now()
        )

        // When
        val copied = original.copy(response = "new response")

        // Then
        copied.id shouldBe original.id
        copied.response shouldBe "new response"
        original.response shouldBe "test response"
    }
})
