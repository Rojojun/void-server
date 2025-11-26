package com.rojojun.voidserver.adapter.out.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("player_sessions")
data class PlayerSessionEntity(
    @Id
    val id: Long? = null,
    val act: Int,
    val logicSealBroken: Boolean,
    val dataSealBroken: Boolean,
    val powerSealBroken: Boolean,
    val endingType: String?,
    val lastActiveAt: Instant
)
