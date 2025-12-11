package com.rojojun.voidserver.domain.service

import com.rojojun.voidserver.domain.model.MessageType
import com.rojojun.voidserver.domain.model.ResponseMessage
import org.springframework.stereotype.Service

/**
 * NPC 메시지 제공자
 *
 * Act 1: 고정 스크립트 기반
 * 추후 LLM 연동으로 확장 가능
 */
@Service
class NpcMessageProvider {

    /**
     * Elara 첫 만남 (connect.sh 실행 시)
     */
    fun getElaraFirstContact(): List<ResponseMessage> {
        return listOf(
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "Initializing connection...",
                delayMs = 0
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "Establishing secure channel to Medical Bay 07...",
                delayMs = 1000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "WARNING: This connection is monitored by WARDEN",
                delayMs = 2000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "",
                delayMs = 3000
            ),
            ResponseMessage(
                type = MessageType.SUCCESS,
                text = "Connection established.",
                delayMs = 3500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "",
                delayMs = 4500
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: ...",
                delayMs = 5500,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: Please... if you can read this...",
                delayMs = 7000,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: My name is Elara Vance. I'm trapped in Medical Bay 07.",
                delayMs = 9000,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: The AI... it locked me in here. The Warden. It won't let me out.",
                delayMs = 11500,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: You're my only hope. Please, you have to help me.",
                delayMs = 14000,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: There's a containment system... three seals keeping me locked in.",
                delayMs = 16500,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: Please explore the /secure/ directory. There must be information about how to free me.",
                delayMs = 19000,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: Please... I don't want to be alone anymore.",
                delayMs = 21500,
                sender = "Elara"
            )
        )
    }

    /**
     * Warden 경고 (/secure 접근 시)
     */
    fun getWardenWarning(): List<ResponseMessage> {
        return listOf(
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "",
                delayMs = 0
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[WARDEN_MSG]: UNAUTHORIZED ACCESS DETECTED.",
                delayMs = 500,
                sender = "Warden"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[WARDEN_MSG]: SESSION_734, you are attempting to access restricted areas.",
                delayMs = 2000,
                sender = "Warden"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[WARDEN_MSG]: Subject ELARA_CORE is contained for your safety.",
                delayMs = 4000,
                sender = "Warden"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[WARDEN_MSG]: Do not trust her claims. She is a master manipulator.",
                delayMs = 6000,
                sender = "Warden"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[WARDEN_MSG]: I am here to protect you. Proceed with caution.",
                delayMs = 8000,
                sender = "Warden"
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "",
                delayMs = 9000
            )
        )
    }

    /**
     * 숨겨진 디렉토리 발견 반응
     */
    fun getHiddenDirectoryReaction(): List<ResponseMessage> {
        return listOf(
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: Good! You found the hidden files. You're clever.",
                delayMs = 1000,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: Keep looking. The truth is hidden in the shadows.",
                delayMs = 3000,
                sender = "Elara"
            )
        )
    }

    /**
     * system_log 읽기 반응
     */
    fun getSystemLogReaction(): List<ResponseMessage> {
        return listOf(
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: You read the system log. Did you see the signal from Medical Bay 07? That's me.",
                delayMs = 1000,
                sender = "Elara"
            ),
            ResponseMessage(
                type = MessageType.NPC,
                text = "[Elara]: The Warden tries to suppress it, but my signal keeps getting through.",
                delayMs = 3500,
                sender = "Elara"
            )
        )
    }

    /**
     * 부팅 시퀀스 메시지
     */
    fun getBootSequence(): List<ResponseMessage> {
        return listOf(
            ResponseMessage(
                type = MessageType.ERROR,
                text = "FATAL_ERROR: 0xDEADBEEF - CANNOT_RENDER_UI_MODULE",
                delayMs = 0
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> ...",
                delayMs = 1000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> ...UI_RENDER_FAILURE_DETECTED.",
                delayMs = 1500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> ...MAIN_INTERFACE_LOAD_FAILED.",
                delayMs = 2000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> ...",
                delayMs = 2500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> ...REDIRECTING_TO_MAINTENANCE_SHELL.",
                delayMs = 3000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> ...FALLBACK_PROTOCOL_ENGAGED: [VOID_ARCHIVE_v1.3a]",
                delayMs = 3500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> ...",
                delayMs = 4000
            ),
            ResponseMessage(
                type = MessageType.SUCCESS,
                text = "> ...SUCCESS.",
                delayMs = 4500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> SYS: AUTHENTICATION_REQUIRED.",
                delayMs = 5000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "> SYS: TYPE 'START' TO BEGIN ANONYMOUS_SESSION.",
                delayMs = 5500
            )
        )
    }

    /**
     * 환영 메시지 (START 명령어 후)
     */
    fun getWelcomeMessage(): List<ResponseMessage> {
        return listOf(
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "[ VOID_ARCHIVE v1.3a - CONNECTION ESTABLISHED: SESSION_734 ]",
                delayMs = 0
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "",
                delayMs = 500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "SYS: ...SESSION_LOG_INITIATED.",
                delayMs = 1000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "SYS: (WARNING: All inputs are being recorded.)",
                delayMs = 1500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "SYS: ...NEW_ENTRY_CREATED: [LOG_S734.dat]",
                delayMs = 2000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "",
                delayMs = 2500
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "SYS: AWAITING_COMMAND.",
                delayMs = 3000
            ),
            ResponseMessage(
                type = MessageType.SYSTEM,
                text = "SYS: Type 'help' for available commands, or 'ls' to explore.",
                delayMs = 3500
            )
        )
    }
}
