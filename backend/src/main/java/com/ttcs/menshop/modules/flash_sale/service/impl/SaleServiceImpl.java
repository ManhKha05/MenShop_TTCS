package com.ttcs.menshop.modules.flash_sale.service.impl;

import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.flash_sale.dto.response.*;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.product_image.entity.ProductImageEntity;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import com.ttcs.menshop.modules.product_variant.repository.ProductVariantRepository;
import com.ttcs.menshop.modules.flash_sale.converter.SaleConverter;
import com.ttcs.menshop.modules.flash_sale.dto.request.SaleRegisterRequest;
import com.ttcs.menshop.modules.flash_sale.dto.request.SaleRequest;
import com.ttcs.menshop.modules.flash_sale.entity.SaleEntity;
import com.ttcs.menshop.modules.flash_sale.repository.SaleRepository;
import com.ttcs.menshop.modules.flash_sale.service.SaleService;
import com.ttcs.menshop.modules.sale_product.entity.SaleProductEntity;
import com.ttcs.menshop.modules.sale_product.repository.SaleProductRepository;
import com.ttcs.menshop.websocket.dto.FlashSaleEvent;
import com.ttcs.menshop.websocket.service.WebSocketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleConverter saleConverter;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SaleProductRepository saleProductRepository;
    private final WebSocketService webSocketService;

    public SaleServiceImpl(SaleRepository saleRepository, SaleConverter saleConverter, ProductRepository productRepository, ProductVariantRepository productVariantRepository, SaleProductRepository saleProductRepository, WebSocketService webSocketService) {
        this.saleRepository = saleRepository;
        this.saleConverter = saleConverter;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.saleProductRepository = saleProductRepository;
        this.webSocketService = webSocketService;
    }

    @Override
    public SaleStatsResponse getSaleStats() {
        Long total = saleRepository.countTotal();
        Long active = saleRepository.countActive();
        Long upcoming = saleRepository.countUpcoming();
        Long ended = saleRepository.countEnded();
        return new  SaleStatsResponse(total, active, upcoming, ended);
    }

    @Override
    public Page<SaleResponse> getAll(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<SaleEntity> entities = saleRepository.search(keyword, status, pageable);
        return entities.map(s -> {
            SaleResponse res= saleConverter.toReponse(s);
            res.setStatus(calculateStatus(s));
            return res;
        });
    }

    @Transactional
    @Override
    public void createSale(SaleRequest request) {
        validateFlashSaleTime(request.getStartTime(), request.getEndTime());

        SaleEntity saleEntity = saleConverter.toEntity(request);
        saleRepository.save(saleEntity);

        webSocketService.sendToTopic("/topic/flash-sale", new FlashSaleEvent("SALE_CREATED"));
    }

    @Override
    public void updateSale(Integer id, SaleRequest saleRequest) {
        SaleEntity saleEntity = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Falsh Sale id: " + id));

        SaleEntity sale = saleConverter.toEntity(saleRequest);
        sale.setId(id);
        sale.setCreatedAt(saleEntity.getCreatedAt());
        sale.setDisabled(saleEntity.isDisabled());
        saleRepository.save(sale);

        webSocketService.sendToTopic("/topic/flash-sale", new FlashSaleEvent("SALE_UPDATED"));
    }

    @Transactional
    @Override
    public void endSale(Integer id) {
        SaleEntity sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy flash sale id: " + id));

        sale.setEndTime(LocalDateTime.now());
        saleRepository.save(sale);

        webSocketService.sendToTopic("/topic/flash-sale", new FlashSaleEvent("SALE_UPDATED"));
    }

    @Transactional
    @Override
    public void disableSale(Integer id) {
        SaleEntity sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy flash sale id: " + id));

        sale.setDisabled(true);
        saleRepository.save(sale);

        webSocketService.sendToTopic("/topic/flash-sale", new FlashSaleEvent("SALE_UPDATED"));
    }


    public String calculateStatus(SaleEntity sale) {
        if (sale.isDisabled()) return "DISABLED";

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(sale.getStartTime())) return "UPCOMING";
        if (now.isAfter(sale.getEndTime())) return "ENDED";
        return "ACTIVE";
    }

    public void validateFlashSaleTime(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        SaleEntity conflict = saleRepository.findOverlapping(start, end);

        if (conflict != null) {
            LocalDateTime nextAvailable = conflict.getEndTime();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

            throw new BadRequestException(
                    "Khoảng thời gian bị trùng với flash sale khác! \n Vui lòng tạo flash sale sau " + nextAvailable.format(fmt)
            );
        }
    }

    @Override
    public Page<SaleProductResponse> getProductsForFlashSale(
            Integer flashSaleId,
            Integer page,
            Integer size,
            String keyword,
            Integer shopId
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductEntity> productPage =
                productRepository.getProductsForFlashSale(shopId, keyword, pageable);

        List<ProductEntity> products = productPage.getContent();

        if (products.isEmpty()) return productPage.map(p -> null);

        List<Integer> productIds = products.stream()
                .map(ProductEntity::getId)
                .toList();

        List<ProductVariantEntity> variants =
                productVariantRepository.findByProductIds(productIds);

        Map<Integer, Integer> stockMap = variants.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getProduct().getId(),
                        Collectors.summingInt(v -> v.getStock() != null ? v.getStock() : 0)
                ));

        List<SaleProductEntity> saleProducts =
                saleProductRepository.findByFlashSaleAndProductIds(flashSaleId, productIds);

        Map<Integer, SaleProductEntity> saleMap = saleProducts.stream()
                .collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(),
                        sp -> sp
                ));

        return productPage.map(p -> {

            String image = p.getImages().stream()
                    .findFirst()
                    .map(ProductImageEntity::getImageUrl)
                    .orElse(null);

            int stock = stockMap.getOrDefault(p.getId(), 0);

            SaleProductEntity sp = saleMap.get(p.getId());

            boolean isRegistered = sp != null;

            BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();

            return new SaleProductResponse(
                    p.getId(),
                    image,
                    p.getName(),
                    price,
                    stock,
                    isRegistered,
                    isRegistered ? sp.getSalePrice() : null
            );
        });
    }

    @Transactional
    @Override
    public void registerProducts(SaleRegisterRequest req) {

        Integer flashSaleId = req.getFlashSaleId();

        // 🔥 Lấy tất cả record hiện tại của flash sale
        List<SaleProductEntity> current =
                saleProductRepository.findByFlashSaleId(flashSaleId);

        Map<Integer, SaleProductEntity> currentMap = current.stream()
                .collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(),
                        sp -> sp
                ));

        for (SaleRegisterRequest.Item item : req.getItems()) {

            Integer productId = item.getProductId();
            SaleProductEntity existing = currentMap.get(productId);

            if (Boolean.TRUE.equals(item.getSelected())) {

                if (item.getFlashPrice() == null) {
                    throw new RuntimeException("Thiếu giá flash");
                }

                if (existing != null) {
                    existing.setSalePrice(item.getFlashPrice());
                } else {
                    SaleProductEntity sp = new SaleProductEntity();
                    sp.setFlashSale(new SaleEntity(flashSaleId));
                    sp.setProduct(new ProductEntity(productId));
                    sp.setSalePrice(item.getFlashPrice());

                    saleProductRepository.save(sp);
                }
            }

            else {
                if (existing != null) {
                    saleProductRepository.delete(existing);
                }
            }
        }

        webSocketService.sendToTopic("/topic/flash-sale", new FlashSaleEvent("SALE_UPDATED"));
    }

    @Override
    public Page<AdminSaleProductResponse> getProductsOfFlashSale(Integer flashSaleId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<SaleProductEntity> pageData =
                saleProductRepository.findProductsByFlashSaleId(flashSaleId, pageable);

        return pageData.map(sp -> {

            ProductEntity p = sp.getProduct();

            String image = p.getImages().isEmpty()
                    ? null
                    : p.getImages().get(0).getImageUrl();

            return new AdminSaleProductResponse(
                    p.getId(),
                    p.getName(),
                    image,
                    p.getShop().getName(),
                    p.getPrice(),
                    sp.getSalePrice()
            );
        });
    }

    @Override
    public SaleHomeResponse getSaleHome() {
        LocalDateTime now = LocalDateTime.now();

        SaleEntity fs = saleRepository.findActiveFlashSale(now);

        if(fs == null){
            return new SaleHomeResponse(
                    null,
                    null,
                    null,
                    null,
                    List.of()
            );
        }
        List<SaleProductEntity> saleProducts = saleProductRepository.findByFlashSaleId(fs.getId());

        List<SaleHomeResponse.Item> items = saleProducts.stream()
                .map(sp -> {
                    ProductEntity p = sp.getProduct();

                    String image = p.getImages().isEmpty()
                            ? null
                            : p.getImages().get(0).getImageUrl();

                    // stock
                    int stock = p.getVariants().stream()
                            .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                            .sum();
                    if (stock == 0) return null;

                    return new SaleHomeResponse.Item(
                            p.getId(),
                            p.getName(),
                            image,
                            p.getPrice(),
                            sp.getSalePrice(),
                            stock
                    );
                })
                .toList();

        return new SaleHomeResponse(
                fs.getId(),
                fs.getName(),
                fs.getStartTime(),
                fs.getEndTime(),
                items
        );
    }

}
