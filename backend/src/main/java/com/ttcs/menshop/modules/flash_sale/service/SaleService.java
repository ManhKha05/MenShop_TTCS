package com.ttcs.menshop.modules.flash_sale.service;

import com.ttcs.menshop.modules.flash_sale.dto.request.SaleRegisterRequest;
import com.ttcs.menshop.modules.flash_sale.dto.request.SaleRequest;
import com.ttcs.menshop.modules.flash_sale.dto.response.*;
import org.springframework.data.domain.Page;

public interface SaleService {
    SaleStatsResponse getSaleStats();
    Page<SaleResponse> getAll(String keyword, String status, int page, int size);
    void createSale(SaleRequest saleRequest);
    void updateSale(Integer id, SaleRequest saleRequest);
    void endSale(Integer id);
    void disableSale(Integer id);
    Page<SaleProductResponse> getProductsForFlashSale(Integer flashSaleId, Integer page, Integer size, String keyword, Integer shopId);
    void registerProducts(SaleRegisterRequest saleRegisterRequest);
    Page<AdminSaleProductResponse> getProductsOfFlashSale(Integer flashSaleId, Integer page, Integer size);
    SaleHomeResponse getSaleHome();
}
