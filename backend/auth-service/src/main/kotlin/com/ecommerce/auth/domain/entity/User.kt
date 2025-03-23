package com.ecommerce.auth.domain.entity

import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(length = 100, nullable = false)
    var firstName: String,

    @Column(length = 100)
    var lastName: String? = null,

    @Column(length = 100, unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(nullable = false)
    var verified: Boolean = false,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<Role> = mutableSetOf(),

    @Column(length = 36, unique = true)
    var verificationToken: String? = null,

    @Column(length = 36, unique = true)
    var resetPasswordToken: String? = null
) : AuditableEntity() {

    fun addRole(role: Role) {
        roles.add(role)
        role.users.add(this)
    }

    fun removeRole(role: Role) {
        roles.remove(role)
        role.users.remove(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
} 