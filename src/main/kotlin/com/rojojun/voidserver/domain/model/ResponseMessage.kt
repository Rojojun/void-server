package com.rojojun.voidserver.domain.model

/**
 * 명령어 실행 결과의 개별 메시지
 *
 * NPC 메시지, 시스템 메시지, 에러 메시지를 구분하여 표현
 * 클라이언트에서 타이핑 효과를 구현할 수 있도록 지연 시간 포함
 */
data class ResponseMessage(
    val type: MessageType,
    val text: String,
    val delayMs: Long = 0, // 클라이언트에서 이 메시지를 표시하기 전 대기 시간
    val sender: String? = null // "Elara", "Warden", null (System)
)

/**
 * 메시지 타입
 */
enum class MessageType {
    SYSTEM,   // 일반 시스템 출력
    NPC,      // NPC 메시지 (Elara, Warden)
    ERROR,    // 에러 메시지
    SUCCESS   // 성공 메시지
}
