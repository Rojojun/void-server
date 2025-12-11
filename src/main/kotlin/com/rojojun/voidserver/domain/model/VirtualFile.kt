package com.rojojun.voidserver.domain.model

import java.time.Instant

/**
 * 가상 파일 시스템의 파일
 *
 * 게임 스토리를 위한 가상 파일을 표현하는 도메인 모델
 */
data class VirtualFile(
    val path: String,
    val name: String,
    val content: String,
    val isDirectory: Boolean = false,
    val isHidden: Boolean = false,
    val isExecutable: Boolean = false,
    val permissions: String = "rw-r--r--",
    val size: Long = content.length.toLong(),
    val createdAt: Instant = Instant.now(),
    val modifiedAt: Instant = Instant.now()
) {
    companion object {
        /**
         * 디렉토리 생성
         */
        fun directory(path: String, name: String, isHidden: Boolean = false): VirtualFile {
            return VirtualFile(
                path = path,
                name = name,
                content = "",
                isDirectory = true,
                isHidden = isHidden,
                permissions = "drwxr-xr-x",
                size = 0
            )
        }

        /**
         * 일반 파일 생성
         */
        fun file(path: String, name: String, content: String, isHidden: Boolean = false): VirtualFile {
            return VirtualFile(
                path = path,
                name = name,
                content = content,
                isHidden = isHidden,
                permissions = "rw-r--r--"
            )
        }

        /**
         * 실행 가능한 스크립트 파일 생성
         */
        fun script(path: String, name: String, content: String): VirtualFile {
            return VirtualFile(
                path = path,
                name = name,
                content = content,
                isExecutable = true,
                permissions = "rwxr-xr-x"
            )
        }
    }
}
