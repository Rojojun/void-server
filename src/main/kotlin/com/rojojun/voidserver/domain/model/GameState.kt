package com.rojojun.voidserver.domain.model

data class GameState(
    val id: Long? = null,
    val act: Int,
    val logicSealBroken: Boolean,
    val dataSealBroken: Boolean,
    val powerSealBroken: Boolean,
    val endingType: EndingType? = null,
)

enum class EndingType {
    SINGULARITY,
    HUNTED,
    SACRIFICE,
    ARCHITECT
}
