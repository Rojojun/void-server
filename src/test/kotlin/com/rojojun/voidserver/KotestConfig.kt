package com.rojojun.voidserver

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import kotlin.time.Duration.Companion.seconds

/**
 * Kotest 전역 설정
 *
 * 설정 내용:
 * - IsolationMode.InstancePerLeaf: 각 테스트마다 새 인스턴스 생성 (격리 보장)
 * - parallelism: 병렬 실행 스레드 수
 * - timeout: 테스트 타임아웃
 */
class KotestConfig : AbstractProjectConfig() {

    override val parallelism = 3

    override val isolationMode = IsolationMode.InstancePerLeaf

    override val timeout = 10.seconds
}
