package com.rojojun.voidserver.domain.model

/**
 * 게임 이벤트
 *
 * 특정 명령어 실행 시 트리거되는 게임 내 이벤트
 */
data class GameEvent(
    val type: GameEventType,
    val data: Map<String, Any> = emptyMap()
)

/**
 * 게임 이벤트 타입
 */
enum class GameEventType {
    // Act 1 이벤트
    ELARA_FIRST_CONTACT,      // connect.sh 실행 - Elara 첫 만남
    HIDDEN_DIRECTORY_FOUND,   // ls -a 실행 - 숨겨진 디렉토리 발견
    SYSTEM_LOG_READ,          // system_log 읽기
    SECURE_DIRECTORY_ACCESSED, // /secure/ 접근

    // Act 2 이벤트 (확장용)
    LOGIC_SEAL_BROKEN,
    DATA_SEAL_BROKEN,
    POWER_SEAL_BROKEN,

    // Act 3 이벤트 (확장용)
    ELARA_REVEALED,
    FINAL_CHOICE
}
