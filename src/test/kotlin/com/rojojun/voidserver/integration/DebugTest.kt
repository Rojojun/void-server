package com.rojojun.voidserver.integration

import com.rojojun.voidserver.domain.port.out.VirtualFileSystemPort
import com.rojojun.voidserver.domain.service.CommandActor
import io.kotest.core.spec.style.FunSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class DebugTest(
    private val commandActor: CommandActor,
    private val fileSystem: VirtualFileSystemPort
) : FunSpec({

    test("debug ls -a command") {
        val sessionId = UUID.randomUUID()
        fileSystem.initializeSession(sessionId, act = 1)

        val result = commandActor.execute(sessionId, "ls -a /secure")
        println("ls -a /secure result:")
        println("  isSuccess: ${result.isSuccess}")
        println("  output: ${result.output}")
        println("  messages count: ${result.messages.size}")
        result.messages.forEachIndexed { idx, msg ->
            println("  message[$idx]: sender=${msg.sender}, text=${msg.text.take(50)}")
        }
    }

    test("debug cat command") {
        val sessionId = UUID.randomUUID()
        fileSystem.initializeSession(sessionId, act = 1)

        println("Available commands: ${commandActor.getAvailableCommands()}")
        println("Commands by intent: ${commandActor.getCommandsByIntent()}")

        val files = fileSystem.listFiles(sessionId, "/", showHidden = false)
        println("Files in root: ${files.map { it.name }}")

        val readmeExists = fileSystem.exists(sessionId, "/readme.txt")
        println("readme.txt exists: $readmeExists")

        val readmeFile = fileSystem.readFile(sessionId, "/readme.txt")
        println("readme.txt content length: ${readmeFile?.content?.length}")

        val result = commandActor.execute(sessionId, "cat readme.txt")
        println("Command result:")
        println("  isSuccess: ${result.isSuccess}")
        println("  exitCode: ${result.exitCode}")
        println("  output: ${result.output}")
        println("  error: ${result.error}")
        println("  messages: ${result.messages}")
    }
})
