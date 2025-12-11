package com.rojojun.voidserver.domain.port.out

import com.rojojun.voidserver.domain.model.VirtualFile
import java.util.UUID

/**
 * 가상 파일 시스템 Port
 *
 * 세션별 독립적인 파일 시스템을 제공하는 아웃바운드 포트
 * 실제 파일 시스템 대신 게임용 가상 파일을 관리
 */
interface VirtualFileSystemPort {
    /**
     * 세션 초기화 (Act별 초기 파일 구조 생성)
     *
     * @param sessionId 세션 ID
     * @param act 현재 Act (1, 2, 3...)
     */
    suspend fun initializeSession(sessionId: UUID, act: Int)

    /**
     * 파일 목록 조회
     *
     * @param sessionId 세션 ID
     * @param path 디렉토리 경로
     * @param showHidden 숨겨진 파일 표시 여부
     * @return 파일 목록
     */
    suspend fun listFiles(sessionId: UUID, path: String, showHidden: Boolean = false): List<VirtualFile>

    /**
     * 파일 읽기
     *
     * @param sessionId 세션 ID
     * @param path 파일 경로
     * @return 파일 객체 (없으면 null)
     */
    suspend fun readFile(sessionId: UUID, path: String): VirtualFile?

    /**
     * 파일 존재 여부 확인
     *
     * @param sessionId 세션 ID
     * @param path 파일 경로
     * @return 존재 여부
     */
    suspend fun exists(sessionId: UUID, path: String): Boolean

    /**
     * 파일 생성/수정
     *
     * @param sessionId 세션 ID
     * @param file 파일 객체
     */
    suspend fun writeFile(sessionId: UUID, file: VirtualFile)

    /**
     * 파일 삭제
     *
     * @param sessionId 세션 ID
     * @param path 파일 경로
     */
    suspend fun deleteFile(sessionId: UUID, path: String)

    /**
     * 스크립트 파일 여부 확인
     *
     * @param sessionId 세션 ID
     * @param path 파일 경로
     * @return 실행 가능 여부
     */
    suspend fun isExecutable(sessionId: UUID, path: String): Boolean
}
