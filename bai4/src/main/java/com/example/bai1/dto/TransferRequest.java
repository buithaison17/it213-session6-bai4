package com.example.bai1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "ID tài khoản nguồn không được trống")
        String senderAccountId,

        @NotBlank(message = "Số tài khoản người nhận không được trống")
        String receiverAccountNumber,

        @NotBlank(message = "Mã ngân hàng không được trống")
        @Pattern(
                regexp = "^(VCB|TCB|MB|BIDV|CTG|ACB|VPB)$",
                message = "Mã ngân hàng không hợp lệ (hỗ trợ: VCB, TCB, MB, BIDV, CTG, ACB, VPB)"
        )
        String bankCode,

        @NotNull(message = "Số tiền không được trống")
        @DecimalMin(
                value = "10000.00",
                inclusive = false,
                message = "Số tiền chuyển khoản phải lớn hơn 10,000 VND"
        )
        BigDecimal amount,

        @Size(max = 255, message = "Nội dung chuyển khoản không được vượt quá 255 ký tự")
        String description
) {
}