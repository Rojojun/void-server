# Reactive Programming Examples with CommandHistoryRepository

## 📚 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Hexagonal Architecture                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [REST API] ─────► [UseCase] ────► [Repository Interface]   │
│   (Adapter)        (Service)         (Port)                  │
│       │                                    │                  │
│       │                                    ▼                  │
│       │                          [Repository Impl]           │
│       │                            (Adapter)                  │
│       │                                    │                  │
│       └────────────────────────────────────┘                  │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### 1. API Endpoints

#### Save Command
```bash
curl -X POST http://localhost:8080/api/commands \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "123e4567-e89b-12d3-a456-426614174000",
    "command": "ls -la",
    "response": "file1.txt\nfile2.txt",
    "intent": "LIST_FILES"
  }'
```

**Response:**
```json
{
  "id": 1,
  "sessionId": "123e4567-e89b-12d3-a456-426614174000",
  "command": "ls -la",
  "response": "file1.txt\nfile2.txt",
  "intent": "LIST_FILES",
  "timestamp": "2025-11-18T10:30:00Z"
}
```

---

#### Get Session History
```bash
curl http://localhost:8080/api/commands/session/123e4567-e89b-12d3-a456-426614174000
```

**Response (JSON Array):**
```json
[
  {
    "id": 1,
    "command": "ls -la",
    "response": "file1.txt",
    "intent": "LIST_FILES",
    "timestamp": "2025-11-18T10:30:00Z"
  },
  {
    "id": 2,
    "command": "cat file1.txt",
    "response": "Hello World",
    "intent": "READ_FILE",
    "timestamp": "2025-11-18T10:31:00Z"
  }
]
```

---

#### Get Recent Commands
```bash
curl "http://localhost:8080/api/commands/session/123e4567-e89b-12d3-a456-426614174000/recent?limit=5"
```

---

#### Search Commands
```bash
curl "http://localhost:8080/api/commands/session/123e4567-e89b-12d3-a456-426614174000/search?keyword=ls"
```

---

#### Get Statistics
```bash
curl http://localhost:8080/api/commands/session/123e4567-e89b-12d3-a456-426614174000/stats
```

**Response:**
```json
{
  "sessionId": "123e4567-e89b-12d3-a456-426614174000",
  "totalCommands": 42,
  "recentCommands": [...]
}
```

---

#### Server-Sent Events (Real-time Stream)
```bash
curl -N http://localhost:8080/api/commands/session/123e4567-e89b-12d3-a456-426614174000/stream \
  -H "Accept: text/event-stream"
```

**Response (SSE Stream):**
```
data: {"id":1,"command":"ls -la",...}

data: {"id":2,"command":"cat file.txt",...}
```

---

## 💡 Reactive Programming Patterns

### Pattern 1: Flow Transformation (Functional)

```kotlin
// Service Layer
fun getSessionHistory(sessionId: UUID): Flow<Command> {
    return flow {
        val commands = commandHistoryRepository.findBySessionId(sessionId)
        commands.forEach { emit(it) }  // Cold stream
    }
}

// Handler에서 사용
val commandsFlow: Flow<CommandResponse> = commandHistoryUseCase
    .getSessionHistory(sessionId)
    .map { CommandResponse.from(it) }  // Transformation
    .filter { it.intent != null }       // Filtering
```

### Pattern 2: Reactive Search with Predicate

```kotlin
override fun searchCommands(sessionId: UUID, keyword: String): Flow<Command> {
    return flow {
        commandHistoryRepository.findBySessionId(sessionId)
            .asFlow()
            .filter { command ->
                // Functional predicate
                command.command.contains(keyword, ignoreCase = true) ||
                command.response.contains(keyword, ignoreCase = true)
            }
            .collect { emit(it) }
    }
}
```

### Pattern 3: Functional Composition

```kotlin
// Extension functions (Functional Programming)
fun Command.withStats(totalCount: Long, rank: Int): Map<String, Any> {
    return mapOf(
        "command" to this,
        "stats" to mapOf(
            "totalCommands" to totalCount,
            "rank" to rank
        )
    )
}

// Usage
val enriched = commands.mapIndexed { index, command ->
    command.withStats(totalCount = commands.size.toLong(), rank = index + 1)
}
```

### Pattern 4: Multi-Paradigm - OOP + Functional

```kotlin
@Service
class CommandHistoryService(
    private val commandHistoryRepository: CommandHistoryRepository  // Dependency Injection (OOP)
) : CommandHistoryUseCase {

    // Functional: High-order function
    override fun getRecentCommands(sessionId: UUID, limit: Int): Flow<Command> {
        return flow {
            commandHistoryRepository
                .findRecentBySessionId(sessionId, limit)
                .forEach { emit(it) }  // Imperative style inside functional context
        }
    }

    // Side-effect handling with 'also'
    override suspend fun saveCommand(command: Command): Command {
        return commandHistoryRepository.save(command)
            .also { saved ->
                println("✅ Saved: ${saved.id}")  // Side-effect
            }
    }
}
```

---

## 🔥 Advanced Examples

### Example 1: WebClient (Client-side Reactive)

```kotlin
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import kotlinx.coroutines.flow.Flow

class CommandHistoryClient(private val webClient: WebClient) {

    suspend fun getSessionHistory(sessionId: UUID): List<CommandResponse> {
        return webClient
            .get()
            .uri("/api/commands/session/$sessionId")
            .retrieve()
            .awaitBody<List<CommandResponse>>()
    }

    suspend fun saveCommand(request: SaveCommandRequest): CommandResponse {
        return webClient
            .post()
            .uri("/api/commands")
            .bodyValue(request)
            .retrieve()
            .awaitBody<CommandResponse>()
    }
}
```

### Example 2: WebSocket Integration

```kotlin
@Component
class CommandWebSocketHandler(
    private val commandHistoryUseCase: CommandHistoryUseCase
) : WebSocketHandler {

    override suspend fun handle(session: WebSocketSession): Unit = coroutineScope {
        val sessionId = UUID.randomUUID()

        // Reactive stream to WebSocket
        commandHistoryUseCase
            .getSessionHistory(sessionId)
            .map { command ->
                session.textMessage(
                    objectMapper.writeValueAsString(CommandResponse.from(command))
                )
            }
            .collect { message ->
                session.send(Mono.just(message)).awaitSingleOrNull()
            }
    }
}
```

### Example 3: Reactive Aggregation

```kotlin
suspend fun getEnrichedStats(sessionId: UUID): SessionStatsResponse {
    // Parallel execution with coroutines
    val count = async { commandHistoryUseCase.getCommandCount(sessionId) }
    val recent = async {
        commandHistoryUseCase.getRecentCommands(sessionId, 5).toList()
    }

    return SessionStatsResponse(
        sessionId = sessionId,
        totalCommands = count.await(),
        recentCommands = recent.await().map { CommandResponse.from(it) }
    )
}
```

### Example 4: Flow Error Handling

```kotlin
fun getSessionHistoryWithErrorHandling(sessionId: UUID): Flow<Command> {
    return flow {
        commandHistoryRepository.findBySessionId(sessionId)
            .forEach { emit(it) }
    }.catch { e ->
        // Error handling in Flow
        logger.error("Error fetching history: $sessionId", e)
        emit(Command(
            sessionId = sessionId,
            command = "ERROR",
            response = e.message ?: "Unknown error",
            intent = CommandIntent.UNKNOWN
        ))
    }.onCompletion {
        logger.info("History stream completed for session: $sessionId")
    }
}
```

---

## 🧪 Testing Examples

### Unit Test with MockK

```kotlin
class CommandHistoryServiceTest : BehaviorSpec({

    val repository = mockk<CommandHistoryRepository>()
    val service = CommandHistoryService(repository)

    Given("세션 ID가 주어졌을 때") {
        val sessionId = UUID.randomUUID()
        val commands = listOf(
            Command(id = 1, sessionId = sessionId, command = "ls", response = "files"),
            Command(id = 2, sessionId = sessionId, command = "cat", response = "content")
        )

        When("getSessionHistory를 호출하면") {
            coEvery { repository.findBySessionId(sessionId) } returns commands

            val result = service.getSessionHistory(sessionId).toList()

            Then("Flow로 변환되어 반환된다") {
                result shouldHaveSize 2
                result[0].command shouldBe "ls"
                result[1].command shouldBe "cat"
            }
        }
    }
})
```

### Integration Test

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommandHistoryApiTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `should save and retrieve command`() {
        val request = SaveCommandRequest(
            sessionId = UUID.randomUUID().toString(),
            command = "ls -la",
            response = "files",
            intent = "LIST_FILES"
        )

        // Save
        val saved = webTestClient
            .post()
            .uri("/api/commands")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody<CommandResponse>()
            .returnResult()
            .responseBody!!

        // Retrieve
        webTestClient
            .get()
            .uri("/api/commands/${saved.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<CommandResponse>()
            .consumeWith { response ->
                response.responseBody!!.command shouldBe "ls -la"
            }
    }
}
```

---

## 📖 Key Concepts

### Reactive Programming
- **Cold Stream**: Flow는 구독할 때만 실행됨 (lazy evaluation)
- **Backpressure**: Flow가 자동으로 처리
- **Non-blocking**: suspend 함수로 비동기 처리

### Functional Programming
- **Immutability**: data class는 불변 객체
- **High-order functions**: map, filter, reduce
- **Function composition**: Extension functions

### Multi-Paradigm
- **OOP**: 클래스, 인터페이스, 상속
- **Functional**: Lambda, High-order functions
- **Reactive**: Flow, suspend functions

---

## 🎯 Best Practices

1. **Use Flow for streams**: 다수의 데이터를 스트리밍할 때
2. **Use suspend for single values**: 단일 값을 반환할 때
3. **Functional transformations**: map, filter 등으로 데이터 변환
4. **Error handling**: catch, onCompletion 사용
5. **Testing**: MockK + Kotest for reactive code

---

## 📚 References

- [Kotlin Coroutines Flow](https://kotlinlang.org/docs/flow.html)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Reactive Programming](https://www.reactivemanifesto.org/)