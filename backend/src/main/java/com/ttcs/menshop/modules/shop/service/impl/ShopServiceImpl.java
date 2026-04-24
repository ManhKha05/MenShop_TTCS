package com.ttcs.menshop.modules.shop.service.impl;

import com.ttcs.menshop.auth.entity.RoleEntity;
import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.RoleRepository;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.notification.service.NotificationService;
import com.ttcs.menshop.modules.shop.converter.ShopConverter;
import com.ttcs.menshop.modules.shop.dto.request.ShopRequest;
import com.ttcs.menshop.modules.shop.dto.request.UpdateStatusShopRequest;
import com.ttcs.menshop.modules.shop.dto.response.ShopResponse;
import com.ttcs.menshop.modules.shop.dto.response.ShopStatsResponse;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import com.ttcs.menshop.modules.shop.service.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShopServiceImpl implements ShopService {

    private final ShopConverter shopConverter;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public ShopServiceImpl(ShopConverter shopConverter, ShopRepository shopRepository, UserRepository userRepository, RoleRepository roleRepository, AuthService authService, NotificationService notificationService) {
        this.shopConverter = shopConverter;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @Transactional
    @Override
    public void createShop(ShopRequest shopRequest) {
        UserEntity user = authService.getCurrentUser();

        ShopEntity shopEntity = shopConverter.toEntity(shopRequest);
        shopEntity.setStatus("PENDING");
        shopEntity.setUser(user);

//        RoleEntity role = roleRepository.findByName("SHOP")
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy quyền SHOP"));
//
//        user.getRoles().add(role);
//
//        userRepository.save(user);
        shopRepository.save(shopEntity);

        UserEntity admin = userRepository.findByRoleName("ADMIN");

        notificationService.createAndSend(
                admin,
                null,
                "NEW_SHOP_REGISTER",
                "Shop mới đăng ký",
                "Shop " + shopEntity.getName() + " đang chờ duyệt"
        );


    }

    @Override
    public ShopResponse getShopProfileByUserId(Integer userId) {
        ShopEntity shopEntity = shopRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Bạn chưa mở cửa hàng"));

        return shopConverter.toResponse(shopEntity);
    }

    @Transactional
    @Override
    public void updateProfileShop(Integer userId, ShopRequest shopRequest) {
        ShopEntity shopEntity = shopRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Bạn chưa có cửa hàng để cập nhật"));

        ShopEntity shop =  shopConverter.toEntity(shopRequest);
        shop.setId(shopEntity.getId());
        shop.setUser(shopEntity.getUser());

        shopRepository.save(shop);
    }

    @Override
    public ShopStatsResponse getShopStats() {
        Long total = shopRepository.count();
        Long request = shopRepository.countByStatus("PENDING");
        Long active = shopRepository.countByStatus("ACTIVE");
        Long banned = shopRepository.countByStatus("BANNED");
        return new ShopStatsResponse(total, request, active, banned);
    }

    @Override
    public Page<ShopResponse> getAllShop(String keyword, String status, String sort, int page, int size) {
        Sort sorting;

        if(sort.equals("newest")) {
            sorting = Sort.by("createdAt").descending();
        } else {
            sorting = Sort.by("createdAt").ascending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<ShopEntity> shopEntities = shopRepository.search(keyword, status, pageable);
        Page<ShopResponse> responses = shopEntities
                .map(shopConverter::toResponse);
        return responses;
    }

    @Override
    public List<ShopResponse> getAllShopsNoPagi() {
        List<ShopEntity> shopEntities = shopRepository.findAll();
        return shopEntities.stream()
                .map(shopConverter::toResponse)
                .toList();
    }

    @Override
    public ShopResponse getShopById(Integer id) {
        ShopEntity shopEntity = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy shop id: " + id));
        return shopConverter.toResponse(shopEntity);
    }

    @Transactional
    @Override
    public void updateStatusShop(Integer id, UpdateStatusShopRequest request) {
        ShopEntity shopEntity = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy shop id: " + id));
        shopEntity.setStatus(request.getStatus());

        if(request.getStatus().equals("ACTIVE")) {
            RoleEntity role = roleRepository.findByName("SHOP").orElse(null);
            UserEntity user = shopEntity.getUser();
            user.getRoles().add(role);
            userRepository.save(user);
        }

        shopRepository.save(shopEntity);
    }
}
