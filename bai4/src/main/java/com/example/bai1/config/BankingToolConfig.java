package com.example.bai1.config;

import com.example.bai1.dto.TransferRequest;
import com.example.bai1.dto.TransferResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class BankingToolConfig {

    private static final Logger log = LoggerFactory.getLogger(BankingToolConfig.class);

    private static final BigDecimal MOCK_INITIAL_BALANCE = new BigDecimal("50000000.00");
    private static final BigDecimal MIN_AMOUNT_LIMIT = new BigDecimal("10000.00");

    // 1. Khai báo Function Bean xử lý Core Banking theo đúng yêu cầu đề bài
    @Bean
    public Function<TransferRequest, TransferResponse> bankTransferFunction() {
        return request -> {
            log.info("[CoreBanking] Nhận yêu cầu giao dịch: Sender={}, Receiver={}, Bank={}, Amount={}, Desc={}",
                    request.senderAccountId(),
                    request.receiverAccountNumber(),
                    request.bankCode(),
                    request.amount(),
                    request.description());

            // 1. Kiểm tra số tiền chuyển tối thiểu
            if (request.amount() == null || request.amount().compareTo(MIN_AMOUNT_LIMIT) <= 0) {
                log.warn("[CoreBanking] Thất bại: Số tiền chuyển không hợp lệ ({})", request.amount());
                return new TransferResponse(
                        null,
                        "FAILED",
                        "Giao dịch thất bại: Số tiền chuyển khoản phải lớn hơn 10,000 VND."
                );
            }

            // 2. Kiểm tra số dư tài khoản nguồn
            if (request.amount().compareTo(MOCK_INITIAL_BALANCE) > 0) {
                log.warn("[CoreBanking] Thất bại: Không đủ số dư (Khả dụng: {}, Cần chuyển: {})",
                        MOCK_INITIAL_BALANCE, request.amount());
                return new TransferResponse(
                        null,
                        "FAILED",
                        String.format("Giao dịch thất bại: Số dư tài khoản nguồn không đủ (Số dư khả dụng: %s VND).", MOCK_INITIAL_BALANCE)
                );
            }

            // 3. Xử lý trừ tiền và tạo mã giao dịch ngẫu nhiên
            String txId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            String successMsg = String.format("Chuyển thành công %s VND đến tài khoản %s ngân hàng %s. Lời nhắn: '%s'",
                    request.amount(),
                    request.receiverAccountNumber(),
                    request.bankCode(),
                    request.description() != null ? request.description() : "");

            log.info("[CoreBanking] Giao dịch thành công. Mã TXN: {}", txId);

            return new TransferResponse(
                    txId,
                    "SUCCESS",
                    successMsg
            );
        };
    }

    // 2. Bọc Function thành ToolCallback để Spring AI nhận diện làm AI Tool
    @Bean("bankTransferTool")
    public ToolCallback bankTransferTool(Function<TransferRequest, TransferResponse> bankTransferFunction) {
        return FunctionToolCallback.builder("bankTransferTool", bankTransferFunction)
                .description("Thực hiện chuyển tiền liên ngân hàng từ tài khoản nguồn senderAccountId "
                        + "sang số tài khoản thụ hưởng receiverAccountNumber tại ngân hàng bankCode. "
                        + "Tự động kiểm tra số dư và sinh mã giao dịch Core Banking.")
                .inputType(TransferRequest.class)
                .build();
    }
}