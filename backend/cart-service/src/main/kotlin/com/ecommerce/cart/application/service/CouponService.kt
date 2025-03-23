package com.ecommerce.cart.application.service

import com.ecommerce.cart.application.dto.CouponCreateRequest
import com.ecommerce.cart.application.dto.CouponDto
import com.ecommerce.cart.application.dto.CouponUpdateRequest
import com.ecommerce.cart.domain.entity.Coupon
import com.ecommerce.cart.domain.exception.EntityNotFoundException
import com.ecommerce.cart.domain.repository.CouponRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class CouponService(private val couponRepository: CouponRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Cacheable(value = ["coupons"], key = "#id")
    fun findById(id: UUID): CouponDto {
        logger.info("Buscando cupom por ID: $id")
        val coupon = couponRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Cupom não encontrado com ID: $id") }
        return toDto(coupon)
    }

    @Cacheable(value = ["coupons"], key = "#code")
    fun findByCode(code: String): CouponDto {
        logger.info("Buscando cupom por código: $code")
        val coupon = couponRepository.findByCode(code)
            .orElseThrow { EntityNotFoundException("Cupom não encontrado com código: $code") }
        return toDto(coupon)
    }

    fun findAll(pageable: Pageable): Page<CouponDto> {
        logger.info("Listando todos os cupons")
        return couponRepository.findAll(pageable).map { toDto(it) }
    }

    fun findAllActive(pageable: Pageable): Page<CouponDto> {
        logger.info("Listando cupons ativos")
        return couponRepository.findByActiveTrue(pageable).map { toDto(it) }
    }

    @Transactional
    @CacheEvict(value = ["coupons"], allEntries = true)
    fun create(request: CouponCreateRequest): CouponDto {
        logger.info("Criando novo cupom com código: ${request.code}")
        
        // Verificar se já existe um cupom com o mesmo código
        if (couponRepository.findByCode(request.code).isPresent) {
            throw IllegalArgumentException("Já existe um cupom com o código: ${request.code}")
        }
        
        val coupon = Coupon(
            code = request.code,
            description = request.description,
            discountType = request.discountType,
            discountValue = request.discountValue,
            minPurchaseAmount = request.minPurchaseAmount,
            maxDiscountAmount = request.maxDiscountAmount,
            maxUses = request.maxUses,
            validFrom = request.validFrom,
            validUntil = request.validUntil,
            active = request.active
        )
        
        val savedCoupon = couponRepository.save(coupon)
        return toDto(savedCoupon)
    }

    @Transactional
    @CacheEvict(value = ["coupons"], key = "#id")
    fun update(id: UUID, request: CouponUpdateRequest): CouponDto {
        logger.info("Atualizando cupom com ID: $id")
        
        val coupon = couponRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Cupom não encontrado com ID: $id") }
        
        // Verificar se o código foi alterado e se já existe outro cupom com o novo código
        if (request.code != coupon.code && couponRepository.findByCode(request.code).isPresent) {
            throw IllegalArgumentException("Já existe um cupom com o código: ${request.code}")
        }
        
        // Atualizar os campos mutáveis do cupom
        val updatedCoupon = coupon.copy(
            code = request.code,
            description = request.description,
            discountType = request.discountType,
            discountValue = request.discountValue,
            minPurchaseAmount = request.minPurchaseAmount,
            maxDiscountAmount = request.maxDiscountAmount,
            maxUses = request.maxUses,
            currentUses = request.currentUses ?: coupon.currentUses,
            validFrom = request.validFrom,
            validUntil = request.validUntil,
            active = request.active,
            updatedAt = LocalDateTime.now()
        )
        
        return toDto(couponRepository.save(updatedCoupon))
    }

    @Transactional
    @CacheEvict(value = ["coupons"], key = "#id")
    fun delete(id: UUID) {
        logger.info("Excluindo cupom com ID: $id")
        
        if (!couponRepository.existsById(id)) {
            throw EntityNotFoundException("Cupom não encontrado com ID: $id")
        }
        
        couponRepository.deleteById(id)
    }

    @Transactional
    @CacheEvict(value = ["coupons"], key = "#id")
    fun toggleActive(id: UUID): CouponDto {
        logger.info("Alternando status do cupom com ID: $id")
        
        val coupon = couponRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Cupom não encontrado com ID: $id") }
        
        val updatedCoupon = coupon.copy(
            active = !coupon.active,
            updatedAt = LocalDateTime.now()
        )
        
        return toDto(couponRepository.save(updatedCoupon))
    }

    fun validateCoupon(code: String): Boolean {
        logger.info("Validando cupom com código: $code")
        
        val couponOptional = couponRepository.findByCode(code)
        
        if (couponOptional.isEmpty) {
            return false
        }
        
        val coupon = couponOptional.get()
        return coupon.isValid()
    }

    fun toDto(coupon: Coupon): CouponDto {
        return CouponDto(
            id = coupon.id,
            code = coupon.code,
            description = coupon.description,
            discountType = coupon.discountType,
            discountValue = coupon.discountValue,
            minPurchaseAmount = coupon.minPurchaseAmount,
            maxDiscountAmount = coupon.maxDiscountAmount,
            maxUses = coupon.maxUses,
            currentUses = coupon.currentUses,
            validFrom = coupon.validFrom,
            validUntil = coupon.validUntil,
            active = coupon.active,
            createdAt = coupon.createdAt,
            updatedAt = coupon.updatedAt
        )
    }
} 