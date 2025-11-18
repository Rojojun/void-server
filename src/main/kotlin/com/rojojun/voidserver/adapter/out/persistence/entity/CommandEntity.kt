package com.rojojun.voidserver.adapter.out.persistence.entity

import com.rojojun.voidserver.domain.model.Command
import com.rojojun.voidserver.domain.model.CommandIntent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * R2DBC 영속성 엔티티 - 명령어 히스토리
 *
 * Kotlin R2DBC 표준:
 * - data class 사용
 * - @Table로 테이블 매핑
 * - @Id는 nullable (새 엔티티는 null, 저장 후 자동 생성)
 * - immutable 객체 (val 사용)
 * - copy() 메서드로 업데이트
 */
@Table("command_history")
data class CommandEntity(
    @Id
    val id: Long? = null,

    @Column("session_id")
    val sessionId: UUID,

    @Column("command")
    val command: String,

    @Column("response")
    val response: String,

    @Column("intent")
    val intent: String? = null,

    @Column("timestamp")
    val timestamp: Instant = Instant.now()
) {
    /**
     * 도메인 모델로 변환
     */
    fun toDomain(): Command = Command(
        id = id,
        sessionId = sessionId,
        command = command,
        response = response,
        intent = intent?.let { CommandIntent.valueOf(it) },
        timestamp = timestamp
    )

    companion object {
        /**
         * 도메인 모델에서 엔티티 생성
         */
        fun from(command: Command): CommandEntity = CommandEntity(
            id = command.id,
            sessionId = command.sessionId,
            command = command.command,
            response = command.response,
            intent = command.intent?.name,
            timestamp = command.timestamp
        )
    }
}
