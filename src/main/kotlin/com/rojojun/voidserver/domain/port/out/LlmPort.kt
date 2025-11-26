package com.rojojun.voidserver.domain.port.out

/**
 * LLM (Large Language Model) 통신 포트
 *
 * 외부 AI 서비스(OpenAI 등)와의 통신을 담당하는 인터페이스.
 * 도메인 로직은 이 인터페이스를 통해 AI의 응답을 요청합니다.
 */
interface LlmPort {
    /**
     * 프롬프트를 전송하고 AI의 응답을 비동기로 수신
     *
     * @param prompt 시스템 프롬프트 및 사용자 입력이 포함된 전체 프롬프트
     * @param contextId 대화 컨텍스트 유지를 위한 세션 ID (선택 사항)
     * @return AI의 응답 텍스트
     */
    suspend fun generateResponse(prompt: String, contextId: Long? = null): String
}
