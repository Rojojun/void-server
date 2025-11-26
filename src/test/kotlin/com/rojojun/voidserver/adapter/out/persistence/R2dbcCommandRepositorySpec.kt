package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.adapter.out.persistence.entity.CommandEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.UUID

/**
 * R2DBC Repository 통합 테스트 (Kotest StringSpec)
 *
 * StringSpec 사용 이유:
 * - 가장 간결한 Spec 스타일
 * - 통합 테스트에 적합 (간단한 검증)
 * - 문자열이 테스트 이름
 */
@DataR2dbcTest
@ActiveProfiles("test")
class R2dbcCommandRepositorySpec(
    private val repository: CommandRepository
) : StringSpec() {

    override fun extensions() = listOf(SpringExtension)

    private lateinit var testSessionId: UUID

    init {
        beforeEach {
            repository.deleteAll()
            testSessionId = UUID.randomUUID()
        }

        "save - 새 엔티티 저장 후 ID 자동 생성" {
            // Given
            val entity = CommandEntity(
                sessionId = testSessionId,
                command = "ls -la",
                response = "file1.txt\nfile2.txt",
                intent = "LIST_FILES"
            )

            // When
            val saved = repository.save(entity)

            // Then
            saved.id.shouldNotBeNull()
            saved.command shouldBe "ls -la"
            saved.response shouldBe "file1.txt\nfile2.txt"
        }

        "findById - 저장한 엔티티 조회" {
            // Given
            val saved = repository.save(
                CommandEntity(
                    sessionId = testSessionId,
                    command = "help",
                    response = "Available commands"
                )
            )

            // When
            val found = repository.findById(saved.id!!)

            // Then
            found.shouldNotBeNull()
            found.id shouldBe saved.id
            found.command shouldBe "help"
        }

        "findBySessionIdOrderByTimestampDesc - 세션의 명령어 히스토리 조회 (최신순)" {
            // Given
            val now = Instant.now()

            repository.save(
                CommandEntity(
                    sessionId = testSessionId,
                    command = "first",
                    response = "first response",
                    timestamp = now.minusSeconds(100)
                )
            )

            repository.save(
                CommandEntity(
                    sessionId = testSessionId,
                    command = "second",
                    response = "second response",
                    timestamp = now.minusSeconds(50)
                )
            )

            repository.save(
                CommandEntity(
                    sessionId = testSessionId,
                    command = "third",
                    response = "third response",
                    timestamp = now
                )
            )

            // When
            val commands = repository.findBySessionIdOrderByTimestampDesc(testSessionId)

            // Then
            commands shouldHaveSize 3
            commands[0].command shouldBe "third"
            commands[1].command shouldBe "second"
            commands[2].command shouldBe "first"
        }

        "findBySessionIdOrderByTimestampDesc - 다른 세션의 데이터는 조회 안됨" {
            // Given
            val otherSessionId = UUID.randomUUID()

            repository.save(
                CommandEntity(
                    sessionId = testSessionId,
                    command = "my command",
                    response = "response"
                )
            )

            repository.save(
                CommandEntity(
                    sessionId = otherSessionId,
                    command = "other command",
                    response = "response"
                )
            )

            // When
            val commands = repository.findBySessionIdOrderByTimestampDesc(testSessionId)

            // Then
            commands shouldHaveSize 1
            commands[0].command shouldBe "my command"
        }

        "findTop10BySessionIdOrderByTimestampDesc - 최근 10개만 조회" {
            // Given
            val now = Instant.now()

            repeat(15) { i ->
                repository.save(
                    CommandEntity(
                        sessionId = testSessionId,
                        command = "command-$i",
                        response = "response-$i",
                        timestamp = now.minusSeconds((15 - i).toLong())
                    )
                )
            }

            // When
            val commands = repository.findTop10BySessionIdOrderByTimestampDesc(testSessionId)

            // Then
            commands shouldHaveSize 10
            commands[0].command shouldBe "command-14"
            commands[9].command shouldBe "command-5"
        }

        "countBySessionId - 세션의 명령어 개수 카운트" {
            // Given
            repeat(5) { i ->
                repository.save(
                    CommandEntity(
                        sessionId = testSessionId,
                        command = "command-$i",
                        response = "response-$i"
                    )
                )
            }

            // When
            val count = repository.countBySessionId(testSessionId)

            // Then
            count shouldBe 5L
        }

        "countBySessionId - 빈 세션은 0" {
            // Given
            val emptySessionId = UUID.randomUUID()

            // When
            val count = repository.countBySessionId(emptySessionId)

            // Then
            count shouldBe 0L
        }

        "delete - 엔티티 삭제" {
            // Given
            val saved = repository.save(
                CommandEntity(
                    sessionId = testSessionId,
                    command = "to be deleted",
                    response = "response"
                )
            )

            // When
            repository.deleteById(saved.id!!)

            // Then
            val found = repository.findById(saved.id!!)
            found.shouldBeNull()
        }

        "findAll - 모든 엔티티 조회" {
            // Given
            repository.save(
                CommandEntity(
                    sessionId = testSessionId,
                    command = "cmd1",
                    response = "res1"
                )
            )

            repository.save(
                CommandEntity(
                    sessionId = UUID.randomUUID(),
                    command = "cmd2",
                    response = "res2"
                )
            )

            // When
            val all = repository.findAll().toList()

            // Then
            all shouldHaveSize 2
        }
    }
}
