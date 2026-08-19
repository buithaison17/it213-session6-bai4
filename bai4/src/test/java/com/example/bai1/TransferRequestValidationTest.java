package com.example.bai1;

import com.example.bai1.dto.TransferRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Thành công khi gửi request với đầy đủ dữ liệu hợp lệ")
    void shouldPassValidationWhenRequestIsValid() {
        TransferRequest validRequest = new TransferRequest(
                "USER_ACC_1001",
                "123456789",
                "MB",
                new BigDecimal("50000.00"),
                "Chuyen tien an trua"
        );

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "Request hợp lệ không được có lỗi vi phạm");
    }

    @Test
    @DisplayName("Bị chặn khi số tiền <= 10,000 VND")
    void shouldFailWhenAmountIsLessThanOrEqualToTenThousand() {
        TransferRequest requestEqualTenThousand = new TransferRequest(
                "USER_ACC_1001",
                "123456789",
                "VCB",
                new BigDecimal("10000.00"), // Vi phạm vì phải LỚN HƠN 10,000 VND
                "Test chuyen tien"
        );

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(requestEqualTenThousand);

        assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Số tiền chuyển khoản phải lớn hơn 10,000 VND");
    }

    @Test
    @DisplayName("Bị chặn khi mã ngân hàng không nằm trong danh sách hỗ trợ")
    void shouldFailWhenBankCodeIsInvalid() {
        TransferRequest requestInvalidBank = new TransferRequest(
                "USER_ACC_1001",
                "123456789",
                "INVALID_BANK", // Không nằm trong regex (VCB|TCB|MB|BIDV|CTG|ACB|VPB)
                new BigDecimal("20000.00"),
                "Test ma ngan hang"
        );

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(requestInvalidBank);

        assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Mã ngân hàng không hợp lệ (hỗ trợ: VCB, TCB, MB, BIDV, CTG, ACB, VPB)");
    }

    @Test
    @DisplayName("Bị chặn với đúng 5 lỗi khi gửi request sai toàn bộ")
    void shouldCaptureAllViolationsWhenRequestIsCompletelyInvalid() {
        TransferRequest badRequest = new TransferRequest(
                "",                             // Lỗi @NotBlank senderAccountId
                "",                             // Lỗi @NotBlank receiverAccountNumber
                "INVALID",                      // Lỗi @Pattern bankCode
                new BigDecimal("5000.00"),       // Lỗi @DecimalMin amount (< 10000)
                "A".repeat(300)                 // Lỗi @Size description (> 255 ký tự)
        );

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(badRequest);

        System.out.println("\n--- LOG VIOLATIONS TỪ JUNIT TEST ---");
        violations.forEach(v -> System.out.printf("[%s] -> %s%n", v.getPropertyPath(), v.getMessage()));

        assertEquals(5, violations.size(), "Phải bắt được chính xác 5 vi phạm");
    }
}