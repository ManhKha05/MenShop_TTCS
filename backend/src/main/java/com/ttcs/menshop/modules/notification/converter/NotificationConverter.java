package com.ttcs.menshop.modules.notification.converter;

import com.ttcs.menshop.modules.notification.dto.NotificationResponse;
import com.ttcs.menshop.modules.notification.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationConverter {

    public NotificationResponse toResponse(NotificationEntity entity) {
        NotificationResponse res = new NotificationResponse();

        res.setId(entity.getId());
        res.setType(entity.getType());
        res.setTitle(entity.getTitle());
        res.setMessage(entity.getMessage());
        res.setIsRead(entity.getIsRead());
        res.setCreatedAt(entity.getCreatedAt());

        if (entity.getOrder() != null) {
            res.setOrderId(entity.getOrder().getId());
            res.setOrderCode(entity.getOrder().getCode());
        }

        return res;
    }
}
