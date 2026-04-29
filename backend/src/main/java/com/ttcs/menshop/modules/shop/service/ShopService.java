package com.ttcs.menshop.modules.shop.service;

import com.ttcs.menshop.modules.shop.dto.request.ShopRequest;
import com.ttcs.menshop.modules.shop.dto.request.UpdateStatusShopRequest;
import com.ttcs.menshop.modules.shop.dto.response.ShopResponse;
import com.ttcs.menshop.modules.shop.dto.response.ShopStatsResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ShopService {
    void createShop(ShopRequest shopRequest);
    ShopResponse getShopProfileByUserId(Integer userId);
    void updateProfileShop(Integer userId, ShopRequest shopRequest);
    ShopStatsResponse getShopStats();
    Page<ShopResponse> getAllShop(String keyword, String status, String sort, int page, int size);
    List<ShopResponse> getAllShopsNoPagi();
    ShopResponse getShopById(Integer shopId, String role);
    void updateStatusShop(Integer id, UpdateStatusShopRequest request);
}
