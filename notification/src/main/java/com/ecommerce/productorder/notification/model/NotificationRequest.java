package com.ecommerce.productorder.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
public class NotificationRequest {
    private NotificationType type;
    private Set<NotificationChannel> channels;
    private String recipient;
    private String subject;
    private String template;
    private Map<String, Object> data;
    private Integer priority;
}

