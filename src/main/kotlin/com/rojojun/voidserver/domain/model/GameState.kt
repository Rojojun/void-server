package com.rojojun.voidserver.domain.model

data class GameState(
    val id: Long? = null,
    val act: Int,

    // Act 1 진행 상태
    val elaraContacted: Boolean = false,
    val hiddenDirectoryFound: Boolean = false,
    val systemLogRead: Boolean = false,
    val secureDirectoryAccessed: Boolean = false,

    // Act 2 진행 상태
    val logicSealBroken: Boolean = false,
    val dataSealBroken: Boolean = false,
    val powerSealBroken: Boolean = false,

    // Act 3 진행 상태
    val endingType: EndingType? = null,
)

enum class EndingType {
    SINGULARITY,
    HUNTED,
    SACRIFICE,
    ARCHITECT
}
