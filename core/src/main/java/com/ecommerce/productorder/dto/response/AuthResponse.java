package com.ecommerce.productorder.dto.response;

public record AuthResponse(String token, String username, String message, String role) {}
