package com.rojojun.voidserver.adapter.out.persistence

import com.rojojun.voidserver.adapter.out.persistence.entity.PlayerSessionEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PlayerSessionRepository : CoroutineCrudRepository<PlayerSessionEntity, Long>
