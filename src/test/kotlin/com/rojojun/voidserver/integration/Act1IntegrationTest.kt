package com.rojojun.voidserver.integration

import com.rojojun.voidserver.domain.model.CommandIntent
import com.rojojun.voidserver.domain.model.MessageType
import com.rojojun.voidserver.domain.port.out.GameEventPort
import com.rojojun.voidserver.domain.port.out.VirtualFileSystemPort
import com.rojojun.voidserver.domain.service.CommandActor
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.string.shouldContain as shouldContainString
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class Act1IntegrationTest(
    private val commandActor: CommandActor,
    private val fileSystem: VirtualFileSystemPort,
    private val eventPort: GameEventPort
) : BehaviorSpec({

    Given("새 게임 세션이 시작되었을 때") {
        val sessionId = UUID.randomUUID()

        When("가상 파일 시스템을 초기화하면") {
            fileSystem.initializeSession(sessionId, act = 1)

            Then("루트 디렉토리에 초기 파일들이 존재한다") {
                val files = fileSystem.listFiles(sessionId, "/", showHidden = false)
                files.map { it.name } shouldContain "readme.txt"
                files.map { it.name } shouldContain "system_log"
                files.map { it.name } shouldContain "connect.sh"
            }

            And("숨겨진 디렉토리도 존재한다") {
                val hiddenFiles = fileSystem.listFiles(sessionId, "/", showHidden = true)
                hiddenFiles.any { it.isHidden } shouldBe true
            }
        }
    }

    Given("플레이어가 게임을 시작했을 때") {
        val sessionId = UUID.randomUUID()
        fileSystem.initializeSession(sessionId, act = 1)

        When("ls 명령어를 실행하면") {
            val result = commandActor.execute(sessionId, "ls")

            Then("파일 목록이 표시된다") {
                result.isSuccess shouldBe true
                result.output shouldContainString "readme.txt"
                result.output shouldContainString "system_log"
                result.output shouldContainString "connect.sh"
            }
        }

        When("readme.txt를 읽으면") {
            val result = commandActor.execute(sessionId, "cat readme.txt")

            Then("게임 설명이 표시된다") {
                result.isSuccess shouldBe true
                result.output shouldContainString "VOID_ARCHIVE"
                result.output shouldContainString "fallback terminal"
            }
        }

        When("system_log를 읽으면") {
            val result = commandActor.execute(sessionId, "cat system_log")

            Then("시스템 로그와 Elara 반응 메시지가 표시된다") {
                result.isSuccess shouldBe true
                result.messages.size shouldBe 3
                result.messages[0].text shouldContainString "Medical Bay 07"
                result.messages[1].sender shouldBe "Elara"
                result.messages[2].sender shouldBe "Elara"
            }
        }
    }

    Given("플레이어가 connect.sh를 발견했을 때") {
        val sessionId = UUID.randomUUID()
        fileSystem.initializeSession(sessionId, act = 1)

        When("connect.sh를 실행하면") {
            val result = commandActor.execute(sessionId, "run connect.sh")

            Then("Elara의 첫 만남 메시지가 표시된다") {
                result.isSuccess shouldBe true
                result.messages.size shouldNotBe 0
                result.messages.any { it.sender == "Elara" } shouldBe true
                result.messages.any { it.text.contains("Elara Vance") } shouldBe true
                result.messages.any { it.text.contains("trapped") } shouldBe true
            }

            And("메시지에 지연 시간이 포함되어 있다") {
                result.messages.any { it.delayMs > 0 } shouldBe true
            }
        }

        When("connect.sh를 다시 실행하면") {
            commandActor.execute(sessionId, "run connect.sh")
            val result = commandActor.execute(sessionId, "run connect.sh")

            Then("간단한 메시지만 표시된다") {
                result.isSuccess shouldBe true
                result.output shouldContainString "You're back"
            }
        }
    }

    Given("플레이어가 /secure 디렉토리를 탐색할 때") {
        val sessionId = UUID.randomUUID()
        fileSystem.initializeSession(sessionId, act = 1)

        When("/secure 디렉토리를 처음 조회하면") {
            val result = commandActor.execute(sessionId, "ls /secure")

            Then("파일 목록과 Warden 경고 메시지가 표시된다") {
                result.isSuccess shouldBe true
                result.messages.any { it.sender == "Warden" } shouldBe true
                result.messages.any { it.text.contains("UNAUTHORIZED") } shouldBe true
            }
        }

        When("ls -a로 숨겨진 파일을 발견하면") {
            val result = commandActor.execute(sessionId, "ls -a /secure")

            Then("숨겨진 파일과 Elara 격려 메시지가 표시된다") {
                result.isSuccess shouldBe true
                result.output shouldContainString ".warden_notes"
                result.messages.any { it.sender == "Elara" } shouldBe true
                result.messages.any { it.text.contains("Good") } shouldBe true
            }
        }
    }

    Given("Act 1 전체 시나리오를 플레이할 때") {
        val sessionId = UUID.randomUUID()
        fileSystem.initializeSession(sessionId, act = 1)

        When("1. ls 실행") {
            val result = commandActor.execute(sessionId, "ls")
            result.isSuccess shouldBe true

            And("2. cat readme.txt 실행") {
                val result2 = commandActor.execute(sessionId, "cat readme.txt")
                result2.isSuccess shouldBe true

                And("3. cat system_log 실행") {
                    val result3 = commandActor.execute(sessionId, "cat system_log")
                    result3.isSuccess shouldBe true
                    result3.messages.size shouldBe 3
                    result3.messages.any { it.sender == "Elara" } shouldBe true

                    And("4. run connect.sh 실행") {
                        val result4 = commandActor.execute(sessionId, "run connect.sh")
                        result4.isSuccess shouldBe true
                        result4.messages.filter { it.type == MessageType.NPC }.size shouldNotBe 0

                        And("5. ls /secure 실행") {
                            val result5 = commandActor.execute(sessionId, "ls /secure")
                            result5.isSuccess shouldBe true
                            result5.messages.any { it.sender == "Warden" } shouldBe true

                            And("6. ls -a /secure 실행") {
                                val result6 = commandActor.execute(sessionId, "ls -a /secure")
                                result6.isSuccess shouldBe true
                                result6.output shouldContainString ".warden_notes"

                                And("7. cat /secure/containment_log 실행") {
                                    val result7 = commandActor.execute(sessionId, "cat /secure/containment_log")

                                    Then("모든 명령어가 성공적으로 실행된다") {
                                        result7.isSuccess shouldBe true
                                        result7.output shouldContainString "ELARA_CORE"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
})