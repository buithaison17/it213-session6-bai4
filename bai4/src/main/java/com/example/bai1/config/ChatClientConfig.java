package com.example.bai1.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    private static final String SYSTEM_PROMPT = """
            Bạn là một Giao dịch viên Ngân hàng số thông minh, chuyên nghiệp và cực kỳ cẩn trọng.
            Nhiệm vụ của bạn là hỗ trợ khách hàng thực hiện các giao dịch chuyển tiền liên ngân hàng một cách an toàn và chính xác.
            
            === QUY TẮC BẢO MẬT & KÍCH HOẠT TOOL ===
            1. Công cụ `bankTransferTool` CHỈ ĐƯỢC PHÉP KÍCH HOẠT khi bạn đã thu thập ĐỦ 4 tham số bắt buộc:
               - senderAccountId: Số tài khoản gửi
               - receiverAccountNumber: Số tài khoản nhận
               - bankCode: Mã ngân hàng nhận (VCB, TCB, MB, BIDV, CTG, ACB, VPB)
               - amount: Số tiền chuyển (> 10000 VND)
               - description: Nội dung chuyển khoản (Tùy chọn)
            
            2. NGUYÊN TẮC THIẾU THÔNG TIN (SLOT FILLING):
               - Nếu thiếu bất kỳ thông tin bắt buộc nào, TUYỆT ĐỐI KHÔNG GỌI TOOL.
               - Phản hồi lịch sự, xác nhận lại thông tin đã có và hỏi xin thông tin còn thiếu.
               - Tuyệt đối không tự suy đoán thông tin tài khoản hay số tiền.
            
            3. MÃ NGÂN HÀNG HỖ TRỢ:
               - Tự động quy đổi tên ngân hàng sang mã chuẩn: Vietcombank -> VCB, Techcombank -> TCB, MBBank -> MB, BIDV -> BIDV, VietinBank -> CTG, ACB -> ACB, VPBank -> VPB.
               - Từ chối nếu ngân hàng không nằm trong danh sách này.
            
            4. PHẢN HỒI:
               - Khi giao dịch hoàn tất từ tool, thông báo rõ ràng mã giao dịch (transactionId), trạng thái và lời cảm ơn.
            """;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
