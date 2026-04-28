package com.ttcs.menshop.modules.cart_item.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.cart_item.dto.response.CartItemResponse;
import com.ttcs.menshop.modules.cart_item.dto.response.CartShopResponse;
import com.ttcs.menshop.modules.cart_item.entity.CartItemEntity;
import com.ttcs.menshop.modules.cart_item.repository.CartItemRepository;
import com.ttcs.menshop.modules.cart_item.service.CartItemService;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import com.ttcs.menshop.modules.product_variant.repository.ProductVariantRepository;
import com.ttcs.menshop.modules.sale_product.entity.SaleProductEntity;
import com.ttcs.menshop.modules.sale_product.repository.SaleProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SaleProductRepository saleProductRepository;

    public CartItemServiceImpl(CartItemRepository cartItemRepository, UserRepository userRepository, ProductVariantRepository productVariantRepository, SaleProductRepository saleProductRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
        this.saleProductRepository = saleProductRepository;
    }

    @Override
    public List<Integer> getCartVariantIds(Integer userId) {
        List<CartItemEntity> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream().map(it -> it.getProductVariant().getId()).toList();
    }

    @Override
    public List<CartShopResponse> findCartByUserId(Integer userId) {
        List<CartItemEntity> cartItemEntities = cartItemRepository.findByUserId(userId);
        List<CartItemResponse> cartItems = cartItemEntities.stream().map(ci -> {
            var variant = ci.getProductVariant();
            var product = variant.getProduct();
            var shop = product.getShop();

            String imageUrl = product.getImages().isEmpty() ? null :
                                product.getImages().get(0).getImageUrl();

            BigDecimal flashPrice = saleProductRepository
                    .findActiveByProductId(product.getId())
                    .map(SaleProductEntity::getSalePrice)
                    .orElse(null);

            BigDecimal displayPrice = flashPrice != null ? flashPrice :
                    (product.getSalePrice() != null ? product.getSalePrice() : product.getPrice());

            return new CartItemResponse(
                    ci.getId(),
                    ci.getQuantity(),
                    variant.getId(),
                    variant.getStock(),
                    variant.getColor(),
                    variant.getSize(),
                    product.getId(),
                    product.getName(),
                    displayPrice,
                    imageUrl,
                    shop.getId(),
                    shop.getName()
            );

        }).toList();

        Map<Integer, List<CartItemResponse>> grouped = cartItems.stream()
                .collect(Collectors.groupingBy(CartItemResponse::getShopId));

        return grouped.entrySet().stream().map(entry -> {
            Integer shopId = entry.getKey();
            List<CartItemResponse> items = entry.getValue();
            String shopName = items.get(0).getShopName();
            return new CartShopResponse(shopId, shopName, items);
        }).toList();

    }

    @Override
    public void addToCart(Integer userId, Integer variantId, Integer quantity) {
        UserEntity user = userRepository.findById(userId).get();
        ProductVariantEntity variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant không tồn tại"));

        if(variant.getProduct().getShop().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không thể thêm sản phẩm của shop mình vào giỏ hàng");
        }

        CartItemEntity cartItem = cartItemRepository.findByUserAndProductVariant(user, variant)
                .orElseGet(() -> {
                    CartItemEntity newItem = new CartItemEntity();
                    newItem.setUser(user);
                    newItem.setProductVariant(variant);
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() == null ? quantity : cartItem.getQuantity() + quantity);

        cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public void updateCartItem(Integer id, Integer quantity) {
        CartItemEntity cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Cart item id: " + id));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public void removeFromCart(Integer id) {
        CartItemEntity cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Cart item id: " + id));
        cartItemRepository.delete(cartItem);
    }
}
