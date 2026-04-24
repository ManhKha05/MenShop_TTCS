package com.ttcs.menshop.modules.dashboard.service;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.modules.dashboard.dto.RecentActivityResponse;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.order.repository.OrderRepository;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardActivityService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<RecentActivityResponse> getRecentActivities() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<RecentActivityResponse> activities = new ArrayList<>();

        List<UserEntity> newUsers = userRepository.findTop5ByOrderByCreatedAtDesc();
        for (UserEntity user : newUsers) {
            activities.add(new RecentActivityResponse(
                    "NEW_USER",
                    (user.getFullName() != null ? user.getFullName() : user.getEmail()) + " vừa đăng ký",
                    user.getCreatedAt().format(formatter)
            ));
        }

        List<ShopEntity> newShops = shopRepository.findTop5ByOrderByCreatedAtDesc();
        for (ShopEntity shop : newShops) {
            activities.add(new RecentActivityResponse(
                    "NEW_SHOP",
                    "Shop " + shop.getName() + " vừa được tạo",
                    shop.getCreatedAt().format(formatter)
            ));
        }

        List<OrderEntity> orders = orderRepository.findTop5ByOrderByCreatedAtDesc();
        for (OrderEntity order : orders) {
            activities.add(new RecentActivityResponse(
                    "NEW_ORDER",
                    "Đơn hàng #" + order.getCode() + " vừa được tạo",
                    order.getCreatedAt().format(formatter)
            ));
        }

        List<ProductEntity> pendingProducts = productRepository.findTop5ByStatusOrderByCreatedAtDesc("PENDING");
        for (ProductEntity product : pendingProducts) {
            activities.add(new RecentActivityResponse(
                    "NEW_PRODUCT_PENDING",
                    "Sản phẩm " + product.getName() + " đang chờ duyệt",
                    product.getCreatedAt().format(formatter)
            ));
        }

        activities.sort(Comparator.comparing(RecentActivityResponse::getCreatedAt).reversed());
        return activities.stream().limit(10).toList();
    }
}
