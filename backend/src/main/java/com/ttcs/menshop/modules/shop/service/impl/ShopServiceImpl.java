package com.ttcs.menshop.modules.shop.service.impl;

import com.ttcs.menshop.auth.entity.RoleEntity;
import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.RoleRepository;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.address.entity.GhnDistrictEntity;
import com.ttcs.menshop.modules.address.entity.GhnProvinceEntity;
import com.ttcs.menshop.modules.address.entity.GhnWardEntity;
import com.ttcs.menshop.modules.address.repository.GhnDistrictRepository;
import com.ttcs.menshop.modules.address.repository.GhnProvinceRepository;
import com.ttcs.menshop.modules.address.repository.GhnWardRepository;
import com.ttcs.menshop.modules.notification.service.NotificationService;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.shop.converter.ShopConverter;
import com.ttcs.menshop.modules.shop.dto.request.ShopRequest;
import com.ttcs.menshop.modules.shop.dto.request.UpdateStatusShopRequest;
import com.ttcs.menshop.modules.shop.dto.response.ShopResponse;
import com.ttcs.menshop.modules.shop.dto.response.ShopStatsResponse;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import com.ttcs.menshop.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopConverter shopConverter;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final NotificationService notificationService;
    private final ProductRepository productRepository;
    private final GhnProvinceRepository ghnProvinceRepository;
    private final GhnDistrictRepository ghnDistrictRepository;
    private final GhnWardRepository ghnWardRepository;

    @Transactional
    @Override
    public void createShop(ShopRequest shopRequest) {
        UserEntity user = authService.getCurrentUser();

        GhnProvinceEntity province = ghnProvinceRepository.findById(shopRequest.getProvinceId())
                .orElseThrow(() -> new BadRequestException("Tỉnh/thành phố không hợp lệ"));

        GhnDistrictEntity district = ghnDistrictRepository.findById(shopRequest.getDistrictId())
                .orElseThrow(() -> new BadRequestException("Quận/huyện không hợp lệ"));

        GhnWardEntity ward = ghnWardRepository.findById(shopRequest.getWardId())
                .orElseThrow(() -> new BadRequestException("Phường/xã không hợp lệ"));

        ShopEntity shopEntity = new ShopEntity();

        shopEntity.setName(shopRequest.getName());
        shopEntity.setDescription(shopRequest.getDescription());
        shopEntity.setLogo(shopRequest.getLogo());
        shopEntity.setPhone(shopRequest.getPhone());
        shopEntity.setEmail(shopRequest.getEmail());

        shopEntity.setStatus("PENDING");
        shopEntity.setUser(user);

        shopEntity.setProvinceId(province.getProvinceId());
        shopEntity.setDistrictId(district.getDistrictId());
        shopEntity.setWardId(ward.getWardCode());
        shopEntity.setDetailAddress(shopRequest.getDetailAddress());

        shopEntity.setAddress(
                shopRequest.getDetailAddress()
                        + ", " + ward.getWardName()
                        + ", " + district.getDistrictName()
                        + ", " + province.getProvinceName()
        );

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
        ShopEntity shop = shopRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Bạn chưa có cửa hàng để cập nhật"));

        GhnProvinceEntity province = ghnProvinceRepository.findById(shopRequest.getProvinceId())
                .orElseThrow(() -> new BadRequestException("Tỉnh/thành phố không hợp lệ"));

        GhnDistrictEntity district = ghnDistrictRepository.findById(shopRequest.getDistrictId())
                .orElseThrow(() -> new BadRequestException("Quận/huyện không hợp lệ"));

        GhnWardEntity ward = ghnWardRepository.findById(shopRequest.getWardId())
                .orElseThrow(() -> new BadRequestException("Phường/xã không hợp lệ"));

        shop.setName(shopRequest.getName());
        shop.setDescription(shopRequest.getDescription());
        shop.setLogo(shopRequest.getLogo());
        shop.setPhone(shopRequest.getPhone());
        shop.setEmail(shopRequest.getEmail());

        shop.setDetailAddress(shopRequest.getDetailAddress());

        shop.setProvinceId(province.getProvinceId());
        shop.setDistrictId(district.getDistrictId());
        shop.setWardId(ward.getWardCode());

        shop.setAddress(
                shopRequest.getDetailAddress()
                        + ", " + ward.getWardName()
                        + ", " + district.getDistrictName()
                        + ", " + province.getProvinceName()
        );

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
    public ShopResponse getShopById(Integer id, String role) {
        ShopEntity shopEntity = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy shop id: " + id));

        if (role.equals("CUSTOMER") && !"ACTIVE".equals(shopEntity.getStatus())) {
            throw new NotFoundException("Shop hiện không hoạt động");
        }

        return shopConverter.toResponse(shopEntity);
    }

    @Transactional
    @Override
    public void updateStatusShop(Integer id, UpdateStatusShopRequest request) {
        ShopEntity shopEntity = shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy shop id: " + id));

        shopEntity.setStatus(request.getStatus());

        if ("ACTIVE".equals(request.getStatus())) {
            RoleEntity role = roleRepository.findByName("SHOP")
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy role SHOP"));

            UserEntity user = shopEntity.getUser();

            boolean hasShopRole = user.getRoles().stream()
                    .anyMatch(r -> "SHOP".equals(r.getName()));

            if (!hasShopRole) {
                user.getRoles().add(role);
                userRepository.save(user);
            }
        }

        shopRepository.save(shopEntity);
    }
}
