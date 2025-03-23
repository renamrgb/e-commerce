package com.ecommerce.catalog.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "slug", unique = true, nullable = false)
    var slug: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "active")
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    val children: MutableSet<Category> = mutableSetOf(),

    @ManyToMany(mappedBy = "categories")
    val products: MutableSet<Product> = mutableSetOf()
) {
    // Métodos para manipulação da hierarquia de categorias
    fun addChild(child: Category) {
        children.add(child)
        child.parent = this
    }

    fun removeChild(child: Category) {
        children.remove(child)
        child.parent = null
    }

    // Método para atualizar a data de modificação
    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Category

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Category(id=$id, name='$name', slug='$slug')"
    }
} 