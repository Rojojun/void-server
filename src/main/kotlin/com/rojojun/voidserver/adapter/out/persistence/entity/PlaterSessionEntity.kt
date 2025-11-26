package com.rojojun.voidserver.adapter.out.persistence.entity

import com.rojojun.voidserver.domain.model.EndingType
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table(name = "plater_session")
data class PlaterSessionEntity(
    val id: Long,

    val act: Int,

    val logicSealBroken: Boolean,

    val dataSealBroken: Boolean,

    val powerSealBroken: Boolean,

    val endingType: EndingType,

    val lastActiveAt: Instant,
) {
}