package com.ecommerce.auth.domain.entity

import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(length = 50, unique = true, nullable = false)
    val name: String,

    @Column(length = 255)
    val description: String? = null,

    @ManyToMany(mappedBy = "roles")
    val users: MutableSet<User> = mutableSetOf()
) : AuditableEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Role) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
} 