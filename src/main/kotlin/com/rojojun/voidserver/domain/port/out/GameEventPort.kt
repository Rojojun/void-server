package com.rojojun.voidserver.domain.port.out

import com.rojojun.voidserver.domain.model.GameEvent
import com.rojojun.voidserver.domain.model.GameEventType
import com.rojojun.voidserver.domain.model.ResponseMessage
import java.util.UUID

/**
 * 게임 이벤트 처리 Port
 */
interface GameEventPort {
    /**
     * 이벤트 처리 후 추가 메시지 반환
     *
     * @param sessionId 세션 ID
     * @param event 발생한 이벤트
     * @return NPC 메시지 등 추가 응답 메시지
     */
    suspend fun handleEvent(sessionId: UUID, event: GameEvent): List<ResponseMessage>

    /**
     * 특정 이벤트가 이미 발생했는지 확인
     *
     * @param sessionId 세션 ID
     * @param eventType 이벤트 타입
     * @return 발생 여부
     */
    suspend fun hasEventOccurred(sessionId: UUID, eventType: GameEventType): Boolean

    /**
     * 이벤트 발생 기록
     *
     * @param sessionId 세션 ID
     * @param event 이벤트
     */
    suspend fun recordEvent(sessionId: UUID, event: GameEvent)
}
