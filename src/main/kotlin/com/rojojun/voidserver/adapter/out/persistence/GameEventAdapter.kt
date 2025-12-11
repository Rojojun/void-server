package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.domain.model.GameEvent
import com.rojojun.voidserver.domain.model.GameEventType
import com.rojojun.voidserver.domain.model.ResponseMessage
import com.rojojun.voidserver.domain.port.out.GameEventPort
import com.rojojun.voidserver.domain.service.NpcMessageProvider
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 게임 이벤트 Adapter (In-Memory 구현)
 *
 * 세션별로 발생한 이벤트를 기록하고, 이벤트에 따른 NPC 메시지 반환
 */
@Component
class GameEventAdapter(
    private val npcProvider: NpcMessageProvider
) : GameEventPort {

    // sessionId -> Set<GameEventType>
    private val sessionEvents = ConcurrentHashMap<UUID, MutableSet<GameEventType>>()

    override suspend fun handleEvent(sessionId: UUID, event: GameEvent): List<ResponseMessage> {
        // 이벤트 기록
        recordEvent(sessionId, event)

        // 이벤트 타입에 따라 NPC 메시지 반환
        return when (event.type) {
            GameEventType.ELARA_FIRST_CONTACT -> npcProvider.getElaraFirstContact()
            GameEventType.SECURE_DIRECTORY_ACCESSED -> npcProvider.getWardenWarning()
            GameEventType.HIDDEN_DIRECTORY_FOUND -> npcProvider.getHiddenDirectoryReaction()
            GameEventType.SYSTEM_LOG_READ -> npcProvider.getSystemLogReaction()
            else -> emptyList()
        }
    }

    override suspend fun hasEventOccurred(sessionId: UUID, eventType: GameEventType): Boolean {
        return sessionEvents[sessionId]?.contains(eventType) ?: false
    }

    override suspend fun recordEvent(sessionId: UUID, event: GameEvent) {
        sessionEvents.getOrPut(sessionId) { mutableSetOf() }.add(event.type)
    }
}
