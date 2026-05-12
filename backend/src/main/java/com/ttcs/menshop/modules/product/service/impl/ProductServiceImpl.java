package com.ttcs.menshop.modules.product.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.brand.entity.BrandEntity;
import com.ttcs.menshop.modules.brand.service.BrandService;
import com.ttcs.menshop.modules.category.entity.CategoryEntity;
import com.ttcs.menshop.modules.category.repository.CategoryRepository;
import com.ttcs.menshop.modules.category.service.CategoryService;
import com.ttcs.menshop.modules.notification.service.NotificationService;
import com.ttcs.menshop.modules.order_item.repository.OrderItemRepository;
import com.ttcs.menshop.modules.product.converter.ProductConverter;
import com.ttcs.menshop.modules.product.dto.request.ProductRequest;
import com.ttcs.menshop.modules.product.dto.response.ProductCardResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductDetailResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductStatsResponse;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.entity.UserInteractionEntity;
import com.ttcs.menshop.modules.product.enums.InteractionType;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.product.repository.UserInteractionRepository;
import com.ttcs.menshop.modules.product.service.ProductService;
import com.ttcs.menshop.modules.product_image.entity.ProductImageEntity;
import com.ttcs.menshop.modules.product_variant.dto.request.ProductVariantRequest;
import com.ttcs.menshop.modules.product_variant.dto.response.ProductVariantResponse;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import com.ttcs.menshop.modules.sale_product.entity.SaleProductEntity;
import com.ttcs.menshop.modules.sale_product.repository.SaleProductRepository;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import com.ttcs.menshop.websocket.dto.ProductRequestEvent;
import com.ttcs.menshop.websocket.dto.ProductSocketEvent;
import com.ttcs.menshop.websocket.service.WebSocketService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BrandService brandService;
    private final ProductConverter  productConverter;
    private final SaleProductRepository saleProductRepository;
    private final WebSocketService webSocketService;
    private final OrderItemRepository orderItemRepository;
    private final AuthService authService;
    private final ShopRepository shopRepository;
    private final CategoryService categoryService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final UserInteractionRepository userInteractionRepository;

    public ProductServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, BrandService brandService, ProductConverter productConverter, SaleProductRepository saleProductRepository, WebSocketService webSocketService, OrderItemRepository orderItemRepository, AuthService authService, ShopRepository shopRepository, CategoryService categoryService, NotificationService notificationService, UserRepository userRepository, UserInteractionRepository userInteractionRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.brandService = brandService;
        this.productConverter = productConverter;
        this.saleProductRepository = saleProductRepository;
        this.webSocketService = webSocketService;
        this.orderItemRepository = orderItemRepository;
        this.authService = authService;
        this.shopRepository = shopRepository;
        this.categoryService = categoryService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.userInteractionRepository = userInteractionRepository;
    }

    @Override
    public ProductStatsResponse getProductStats(Integer shopId) {
        List<Object[]> result = productRepository.getProductStats(shopId);
        Object[] statsArray = result.get(0);

        return ProductStatsResponse.builder()
                .totalProducts((Long) statsArray[0])
                .pending((Long) statsArray[1])
                .active((Long) statsArray[2])
                .inactive((Long) statsArray[3])
                .outOfStock((Long) statsArray[4])
                .build();
    }

    @Override
    public Page<ProductResponse> getProducts(Integer page, Integer size, String keyword, Integer categoryId, String status, Integer shopId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ProductEntity> productPage = productRepository.search(keyword, categoryId, status, shopId, pageable);

        return productPage.map(productConverter::mapToResponse);
    }

    @Override
    public ProductDetailResponse getProductDetail(Integer id) {
        ProductEntity p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found"));

        UserEntity user = authService.getCurrentUserOrNull();
        boolean isCustomer = false;

        if (user != null) {
            isCustomer = user.getRoles().size() == 1
                    && user.getRoles().stream()
                    .anyMatch(r -> "CUSTOMER".equals(r.getName()));
        }

        if(isCustomer){
            if(!p.getShop().getStatus().equals("ACTIVE")){
                throw new NotFoundException("Sản phẩm bị ẩn");
            }

            p.setViewCount(p.getViewCount() + 1);
            productRepository.save(p);

            UserInteractionEntity userInteractionEntity = UserInteractionEntity.builder()
                    .user(user)
                    .product(p)
                    .interactionType(InteractionType.VIEW)
                    .weightScore(BigDecimal.valueOf(1.0))
                    .build();

            userInteractionRepository.save(userInteractionEntity);
        }

        ProductDetailResponse res = new ProductDetailResponse();

        res.setId(p.getId());
        res.setName(p.getName());
        res.setPrice(p.getPrice());
        res.setSalePrice(p.getSalePrice());
        res.setDescription(p.getDescription());
        res.setHeight(p.getHeight());
        res.setWeight(p.getWeight());
        res.setLength(p.getLength());
        res.setWidth(p.getWidth());
        res.setStatus(p.getStatus());
        res.setShopId(p.getShop().getId());
        res.setShopName(p.getShop().getName());
        res.setShopImg(p.getShop().getLogo());
        res.setSoldCount(p.getSoldCount());
        res.setViewCount(p.getViewCount());
        res.setRatingAvg(p.getRatingAvg());

        res.setCategoryId(p.getCategory().getId());

        if(p.getBrand() != null) {
            res.setBrandName(p.getBrand().getName());
        }

        res.setAttributesJson(p.getAttributesJson());

        // images
        res.setImages(
                p.getImages().stream()
                        .map(img -> img.getImageUrl())
                        .toList()
        );

        // variants
        res.setVariants(
                p.getVariants().stream().map(v -> {
                    ProductVariantResponse dto = new ProductVariantResponse();
                    dto.setId(v.getId());
                    dto.setSku(v.getSku());
                    dto.setSize(v.getSize());
                    dto.setColor(v.getColor());
                    dto.setStock(v.getStock());
                    return dto;
                }).toList()
        );

        SaleProductEntity sale = p.getSaleProducts().stream()
                .filter(sp -> {
                    var fs = sp.getFlashSale();
                    return !fs.isDisabled()
                            && fs.getStartTime().isBefore(java.time.LocalDateTime.now())
                            && fs.getEndTime().isAfter(java.time.LocalDateTime.now());
                })
                .findFirst()
                .orElse(null);

        if (sale != null) {
            res.setFlashPrice(sale.getSalePrice());
        } else {
            res.setFlashPrice(null);
        }

        return res;
    }

    @Override
    public void create(ProductRequest productRequest) {
        ProductEntity product = new ProductEntity();
        mapCommon(product, productRequest);
        int shopId = authService.getCurrentUser().getShop().getId();
        ShopEntity shop = shopRepository.findById(shopId).orElse(null);
        product.setShop(shop);
        product.setStatus("PENDING");
        productRepository.save(product);

        UserEntity admin = userRepository.findByRoleName("ADMIN");

        notificationService.createAndSend(
                admin,
                null,
                "NEW_PRODUCT_PENDING",
                "Có sản phẩm cần duyệt",
                "Shop " + shop.getName() + " vừa tạo sản phẩm mới"
        );

        webSocketService.sendToTopic("/topic/product/admin", new ProductRequestEvent("PRODUCT_REQUEST_CREATE"));
    }

    @Override
    public void update(Integer id, ProductRequest productRequest) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow();
        mapCommon(product, productRequest);
//        if (product.getStatus().equals("ACTIVE")) {
//            product.setStatus("PENDING");
//        }
        product.setStatus(productRequest.getStatus());
        productRepository.save(product);

        webSocketService.sendToTopic(
                "/topic/products",
                            new ProductSocketEvent("PRODUCT_UPDATED", product.getId()));
    }

    private void mapCommon(ProductEntity product, ProductRequest req) {

        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setSalePrice(req.getSalePrice());
        product.setDescription(req.getDescription());
        product.setWeight(req.getWeight());
        product.setHeight(req.getHeight());
        product.setWidth(req.getWidth());
        product.setLength(req.getLength());
        product.setAttributesJson(req.getAttributesJson());
        product.setUpdatedAt(LocalDateTime.now());

        // category
        CategoryEntity category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow();
        product.setCategory(category);

        BrandEntity brand = brandService.getOrCreateBrand(req.getBrandName());
        product.setBrand(brand);

        // images
        product.getImages().clear();
        for (String url : req.getImages()) {
            ProductImageEntity img = new ProductImageEntity();
            img.setImageUrl(url);
            img.setProduct(product);
            product.getImages().add(img);
        }

        // variants
        syncVariants(product, req.getVariants());
    }

    private void syncVariants(ProductEntity product, List<ProductVariantRequest> requests) {
        Map<Integer, ProductVariantEntity> existingMap = product.getVariants().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(ProductVariantEntity::getId, v -> v));

        Set<Integer> incomingIds = new HashSet<>();

        List<ProductVariantEntity> newList = new ArrayList<>();

        for (ProductVariantRequest req : requests) {
            ProductVariantEntity variant;

            if (req.getId() != null && existingMap.containsKey(req.getId())) {
                variant = existingMap.get(req.getId());
                incomingIds.add(req.getId());
            } else {
                variant = new ProductVariantEntity();
                variant.setProduct(product);
            }

            variant.setColor(req.getColor());
            variant.setSize(req.getSize());
            variant.setStock(req.getStock());
            variant.setSku(generateSku(product.getName(), req.getColor(), req.getSize()));

            newList.add(variant);
        }

        // không clear bừa rồi xóa cứng
        // chỉ remove variant chưa dùng ở order
        List<ProductVariantEntity> toRemove = product.getVariants().stream()
                .filter(v -> v.getId() != null && !incomingIds.contains(v.getId()))
                .toList();

        for (ProductVariantEntity old : toRemove) {
            boolean usedInOrder = orderItemRepository.existsByVariantId(old.getId());
            if (usedInOrder) {
                old.setStock(0);
                newList.add(old);
            }
            // nếu chưa từng dùng thì có thể bỏ hẳn
        }

        product.getVariants().clear();
        product.getVariants().addAll(newList);
    }

    private String generateSku(String productName, String color, String size) {
        String productCode = normalize(productName);
        String colorCode = normalize(color);
        String sizeCode = normalize(size);

        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return productCode + "-" + colorCode + "-" + sizeCode + "-" + random;
    }

    private String normalize(String input) {
        if (input == null) return "XXX";

        String cleaned = input
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        if (cleaned.length() >= 3) {
            return cleaned.substring(0, 3);
        }

        // pad thêm ký tự
        return String.format("%-3s", cleaned).replace(' ', 'X');
    }

    public void approve(Integer id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!product.getStatus().equals("PENDING")) {
            throw new RuntimeException("Chỉ sản phẩm PENDING mới được duyệt");
        }
        product.setStatus("ACTIVE");
        productRepository.save(product);

        UserEntity shopOwner = product.getShop().getUser();

        notificationService.createAndSend(
                shopOwner,
                null,
                "PRODUCT_APPROVED",
                "Sản phẩm đã được duyệt",
                "Sản phẩm \"" + product.getName() + "\" đã được admin duyệt"
        );

        webSocketService.sendToTopic("/topic/shop-products/" + product.getShop().getId(),
                new ProductSocketEvent("PRODUCT_UPDATED", product.getId()));
    }

    @Transactional
    public void reject(Integer id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!product.getStatus().equals("PENDING")) {
            throw new RuntimeException("Chỉ sản phẩm PENDING mới được từ chối");
        }

        product.setStatus("REJECTED");
        productRepository.save(product);

        UserEntity shopOwner = product.getShop().getUser();

        System.out.println("REJECT gửi tới: " + shopOwner.getId());

        notificationService.createAndSend(
                shopOwner,
                null,
                "PRODUCT_REJECTED",
                "Sản phẩm bị từ chối",
                "Sản phẩm \"" + product.getName() + "\" chưa được duyệt"
        );
    }

    @Override
    public void active(Integer id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        if (!product.getStatus().equals("INACTIVE")) {
            throw new RuntimeException("Chỉ sản phẩm INACTIVE mới được mở khóa");
        }
        product.setStatus("ACTIVE");
        productRepository.save(product);
    }

    public void inactive(Integer id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        if (!product.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Chỉ sản phẩm ACTIVE mới bị khóa");
        }
        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    @Override
    public List<ProductCardResponse> getBestSellingProductsByShop(Integer shopId) {
        Pageable pageable = PageRequest.of(0, 6);
        List<ProductEntity> products = productRepository.findBestSellingProductsByShop(shopId, pageable);
        return products.stream()
                .map(p -> {
                    String image = p.getImages().get(0).getImageUrl();

                    BigDecimal flashSalePrice = saleProductRepository.findActiveByProductId(p.getId())
                            .map(s -> s.getSalePrice())
                            .orElse(null);

                    BigDecimal salePrice = flashSalePrice != null ? flashSalePrice : p.getSalePrice();

                    return new ProductCardResponse(
                            p.getId(),
                            p.getName(),
                            image,
                            p.getPrice(),
                            salePrice,
                            BigDecimal.valueOf(0),
                            "",
                            p.getRatingAvg(),
                            p.getSoldCount()
                    );
                })
                .toList();
    }

    @Override
    public Page<ProductCardResponse> getProductsByShop(Integer shopId, Integer page, Integer size, Integer category, String sort, String priceOrder) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> productEntities = productRepository.getProductsForShop(shopId, category, sort, priceOrder, pageable);
        return productEntities.map(p -> {
            String image = p.getImages().get(0).getImageUrl();

            BigDecimal flashSalePrice = saleProductRepository.findActiveByProductId(p.getId())
                    .map(s -> s.getSalePrice())
                    .orElse(null);

            BigDecimal salePrice = flashSalePrice != null ? flashSalePrice : p.getSalePrice();

            return new ProductCardResponse(
                    p.getId(),
                    p.getName(),
                    image,
                    p.getPrice(),
                    salePrice,
                    BigDecimal.valueOf(0),
                    "",
                    p.getRatingAvg(),
                    p.getSoldCount()
            );
        });
    }

    public Page<ProductCardResponse> getProductsByCategory(
            Integer categoryId,
            int page,
            int size,
            String sort,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer rating,
            List<Integer> childCategoryIds
    ) {
        CategoryEntity category = categoryRepository.findActiveById(categoryId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục id: " + categoryId));

        List<Integer> categoryIds;

        if (childCategoryIds != null && !childCategoryIds.isEmpty()) {
            categoryIds = childCategoryIds;
        } else {
            categoryIds = categoryService.getCategoryIdsForProductQuery(category);
        }

        Pageable pageable = PageRequest.of(page, size, getSort(sort));

        return productRepository.searchByCategoryIds(
                categoryIds,
                "ACTIVE",
                minPrice,
                maxPrice,
                rating,
                pageable
        );
    }

    private Sort getSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "soldCount");
        }

        return switch (sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "id");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "salePrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "salePrice");
            case "best_selling" -> Sort.by(Sort.Direction.DESC, "soldCount");
            case "rating" -> Sort.by(Sort.Direction.DESC, "ratingAvg");
            default -> Sort.by(Sort.Direction.DESC, "soldCount");
        };
    }

}
