# Testing Guide - Kotest 기반 TDD

## 📚 Kotest Spec 전략

프로젝트에서는 **테스트 유형에 따라 최적의 Spec**을 사용합니다.

### 1️⃣ DescribeSpec - 도메인 모델 테스트

**사용 대상**: Domain Model, Value Object, Enum

**특징**:
- BDD 스타일 (Behavior-Driven Development)
- `describe` - `context` - `it` 계층 구조
- 도메인 객체의 다양한 행동을 명확히 표현

**예시**:
```kotlin
class CommandSpec : DescribeSpec({
    describe("Command 생성") {
        context("필수 필드만 제공하면") {
            it("기본값으로 Command가 생성된다") {
                val command = Command(...)
                command.id.shouldBeNull()
            }
        }

        context("모든 필드를 제공하면") {
            it("지정한 값으로 Command가 생성된다") {
                // ...
            }
        }
    }
})
```

**장점**:
- 계층적 구조로 관련 테스트 그룹화
- 읽기 쉬운 테스트 명세
- 도메인 언어로 테스트 표현

---

### 2️⃣ FunSpec - Entity 변환 테스트

**사용 대상**: Entity ↔ Domain 변환, Mapper, Converter

**특징**:
- 함수형 스타일
- `test("테스트 이름")` 단순 구조
- 간결하고 직관적

**예시**:
```kotlin
class CommandEntitySpec : FunSpec({
    test("CommandEntity to Domain 변환 - intent null") {
        // Given
        val entity = CommandEntity(...)

        // When
        val domain = entity.toDomain()

        // Then
        domain.id shouldBe 1L
    }

    test("Domain to CommandEntity 변환 - intent 포함") {
        // ...
    }
})
```

**장점**:
- 단순한 변환 로직 테스트에 최적
- 보일러플레이트 최소화
- 빠른 테스트 작성

---

### 3️⃣ StringSpec - Repository 통합 테스트

**사용 대상**: Repository, DB 통합 테스트, API 통합 테스트

**특징**:
- **가장 간결한** Spec
- 문자열이 테스트 이름
- 통합 테스트에 적합

**예시**:
```kotlin
@DataR2dbcTest
@ActiveProfiles("test")
class R2dbcCommandRepositorySpec(
    private val repository: R2dbcCommandRepository
) : StringSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        "save - 새 엔티티 저장 후 ID 자동 생성" {
            val saved = repository.save(entity)
            saved.id.shouldNotBeNull()
        }

        "findById - 저장한 엔티티 조회" {
            // ...
        }
    }
}
```

**장점**:
- 최소한의 코드로 테스트 작성
- 통합 테스트의 복잡성 숨김
- 읽기 쉬운 테스트 리포트

---

### 4️⃣ BehaviorSpec - Adapter/Service 단위 테스트

**사용 대상**: Adapter, Service, UseCase (MockK 사용)

**특징**:
- **Given-When-Then** 명확히 분리
- 비즈니스 행동(Behavior) 검증
- MockK와 완벽한 조합

**예시**:
```kotlin
class CommandHistoryRepositoryAdapterSpec : BehaviorSpec({
    val r2dbcRepository = mockk<R2dbcCommandRepository>()
    val adapter = CommandHistoryRepositoryAdapter(r2dbcRepository)

    Given("도메인 Command가 주어졌을 때") {
        val domainCommand = Command(...)

        When("save를 호출하면") {
            coEvery { r2dbcRepository.save(any()) } returns savedEntity
            val result = adapter.save(domainCommand)

            Then("엔티티로 변환되어 저장된다") {
                result.id.shouldNotBeNull()
                coVerify(exactly = 1) { r2dbcRepository.save(any()) }
            }
        }
    }
})
```

**장점**:
- Given-When-Then이 코드에 명시적으로 드러남
- 복잡한 비즈니스 로직 테스트에 적합
- Mock 검증이 자연스러움

---

## 🎯 Spec 선택 가이드

| 테스트 대상 | 추천 Spec | 이유 |
|----------|----------|-----|
| Domain Model | `DescribeSpec` | 계층적 BDD 스타일 |
| Entity/Mapper | `FunSpec` | 단순한 함수형 테스트 |
| Repository | `StringSpec` | 간결한 통합 테스트 |
| Adapter/Service | `BehaviorSpec` | Given-When-Then 명확 |
| Controller | `FreeSpec` | 자유로운 구조 |

---

## 🛠️ Kotest Matchers 사용법

### 기본 Assertion

```kotlin
// JUnit 스타일 (❌)
assertEquals(expected, actual)
assertNotNull(value)

// Kotest 스타일 (✅)
actual shouldBe expected
value.shouldNotBeNull()
```

### Null 검증

```kotlin
value.shouldBeNull()
value.shouldNotBeNull()
```

### Collection 검증

```kotlin
list shouldHaveSize 3
list shouldContain "item"
list.shouldBeEmpty()
list.shouldContainAll("a", "b", "c")
```

### 예외 검증

```kotlin
shouldThrow<IllegalArgumentException> {
    // 예외 발생 코드
}

shouldNotThrow<Exception> {
    // 예외 없는 코드
}
```

### 숫자 검증

```kotlin
value shouldBeGreaterThan 10
value shouldBeLessThan 100
value shouldBeInRange 1..10
```

---

## 🧪 MockK 사용법 (Coroutine)

### Mock 생성

```kotlin
val repository = mockk<R2dbcCommandRepository>()
```

### Stub 설정 (suspend fun)

```kotlin
coEvery { repository.save(any()) } returns savedEntity
coEvery { repository.findById(1L) } returns entity
```

### 검증 (suspend fun)

```kotlin
coVerify(exactly = 1) { repository.save(any()) }
coVerify(atLeast = 1) { repository.findById(any()) }
coVerify { repository.save(match { it.command == "ls" }) }
```

---

## 🔧 Spring 통합 테스트

### R2DBC Repository Test

```kotlin
@DataR2dbcTest
@ActiveProfiles("test")
class RepositorySpec(
    private val repository: MyRepository
) : StringSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        beforeEach {
            repository.deleteAll()
        }

        "테스트 케이스" {
            // ...
        }
    }
}
```

### WebFlux Controller Test

```kotlin
@WebFluxTest(GameController::class)
class GameControllerSpec(
    private val webTestClient: WebTestClient
) : FunSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        test("POST /api/command") {
            webTestClient.post()
                .uri("/api/command")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
        }
    }
}
```

---

## 📊 테스트 실행

### 전체 테스트 실행

```bash
./gradlew test
```

### 특정 Spec만 실행

```bash
./gradlew test --tests "*CommandSpec"
./gradlew test --tests "*RepositorySpec"
```

### 테스트 리포트 확인

```bash
# HTML 리포트
open build/reports/tests/test/index.html

# 콘솔 출력
./gradlew test --info
```

---

## 🎨 테스트 작성 베스트 프랙티스

### 1. Given-When-Then 주석 사용

```kotlin
test("save - 새 엔티티 저장") {
    // Given
    val entity = CommandEntity(...)

    // When
    val saved = repository.save(entity)

    // Then
    saved.id.shouldNotBeNull()
}
```

### 2. 명확한 테스트 이름 (한글 가능)

```kotlin
✅ "save - 새 엔티티 저장 후 ID 자동 생성"
✅ "findById - 존재하지 않으면 null 반환"
❌ "test1"
❌ "testSave"
```

### 3. 테스트 격리 (각 테스트는 독립적)

```kotlin
init {
    beforeEach {
        // 테스트 전 초기화
        repository.deleteAll()
    }

    afterEach {
        // 테스트 후 정리
    }
}
```

### 4. 한 테스트에 하나의 검증

```kotlin
// ❌ 여러 개념 테스트
test("save and find") {
    val saved = repository.save(entity)
    val found = repository.findById(saved.id)
    // ...
}

// ✅ 분리
test("save - 엔티티 저장") { /* ... */ }
test("findById - 엔티티 조회") { /* ... */ }
```

---

## 📈 테스트 커버리지 목표

- **Domain Layer**: 100% (비즈니스 로직)
- **Application Layer**: 90%+ (Use Case)
- **Adapter Layer**: 80%+ (통합 테스트)
- **전체**: 85%+

---

## 🚀 다음 단계

1. ✅ Command 엔티티 테스트 완료
2. ⬜ PlayerSession 엔티티 테스트
3. ⬜ VirtualFile 엔티티 테스트
4. ⬜ Use Case 테스트
5. ⬜ Controller 테스트

---

## 📚 참고 자료

- [Kotest 공식 문서](https://kotest.io/)
- [MockK 공식 문서](https://mockk.io/)
- [Spring Boot R2DBC Testing](https://docs.spring.io/spring-data/r2dbc/reference/testing.html)
