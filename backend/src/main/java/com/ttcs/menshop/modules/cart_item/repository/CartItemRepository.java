package com.ttcs.menshop.modules.cart_item.repository;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.cart_item.entity.CartItemEntity;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Integer> {
    List<CartItemEntity> findByUserId(Integer userId);
    Optional<CartItemEntity> findByUserAndProductVariant(UserEntity user, ProductVariantEntity productVariant);
    List<CartItemEntity> findByIdIn(List<Integer> ids);
}
