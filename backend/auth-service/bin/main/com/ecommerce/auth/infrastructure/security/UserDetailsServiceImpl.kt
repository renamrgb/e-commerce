package com.ecommerce.auth.infrastructure.security

import com.ecommerce.auth.domain.entity.User
import com.ecommerce.auth.domain.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDetailsServiceImpl(private val userRepository: UserRepository) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("Usuário não encontrado com o email: $email") }
        
        return buildUserDetails(user)
    }

    private fun buildUserDetails(user: User): UserDetails {
        val authorities = user.roles.map { role -> SimpleGrantedAuthority(role.name) }

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(authorities)
            .disabled(!user.active)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .build()
    }
} 