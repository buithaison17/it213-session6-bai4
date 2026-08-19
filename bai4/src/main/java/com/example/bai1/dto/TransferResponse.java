package com.example.bai1.dto;

public record TransferResponse(
        String transactionId,
        String status,
        String message
) {
}
