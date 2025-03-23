package com.ecommerce.auth.domain.repository

import com.ecommerce.auth.domain.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByName(name: String): Optional<Role>
    fun existsByName(name: String): Boolean
} 