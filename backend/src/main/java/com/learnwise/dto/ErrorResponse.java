package com.learnwise.dto;

import java.time.Instant;

public record ErrorResponse(String code, String message, Instant timestamp, String requestId) {}
