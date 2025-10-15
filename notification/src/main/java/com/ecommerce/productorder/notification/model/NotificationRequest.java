package com.ecommerce.productorder.notification.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class NotificationRequest {
    private NotificationType type;
    private Set<NotificationChannel> channels;
    private String recipient;
    private String subject;
    private String template;
    private Map<String, Object> data;
    private Integer priority;
    
    public NotificationRequest() {}
    
    public NotificationRequest(NotificationType type, Set<NotificationChannel> channels, String recipient,
                              String subject, String template, Map<String, Object> data, Integer priority) {
        this.type = type;
        this.channels = channels;
        this.recipient = recipient;
        this.subject = subject;
        this.template = template;
        this.data = data;
        this.priority = priority;
    }
}

