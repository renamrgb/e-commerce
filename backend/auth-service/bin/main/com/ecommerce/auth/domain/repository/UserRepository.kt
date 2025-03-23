package com.ecommerce.auth.domain.repository

import com.ecommerce.auth.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findByVerificationToken(token: String): Optional<User>
    fun findByResetPasswordToken(token: String): Optional<User>
} 