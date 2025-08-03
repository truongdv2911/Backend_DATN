package com.example.demo.Service;

import com.example.demo.DTOs.SearchRequestDTO;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.San_pham_Repo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.metadata.OpenAiChatResponseMetadata;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAiChatClient aiClient; // Spring AI
    private final San_pham_Repo sanPhamRepo;

    public List<SanPham> handleUserInput(String userInput) {
        // Tạo prompt phân tích ý định
        String prompt = """
                    Bạn là trợ lý thông minh chuyên giúp khách hàng tìm đồ chơi LEGO.
                                
                    Hãy phân tích câu sau và trả về JSON như sau:
                {
                    "ten": "lego sieu xe",
                    "gia": 1000000,
                  "doTuoi": "6-12",
                  "xuatXu": "Đan Mạch",
                  "thuongHieu": "LEGO SPEED CHAMPIONS",
                  "boSuuTap": "LEGO SPEED CHAMPIONS ALL",
                  "soLuongManhGhepMin": 500,
                 "danhGiaToiThieu": 5 sao,
                }
                    Chỉ trả lại tên hàm phù hợp với thông tin khách hỏi.
                    Câu: "%s"
                """.formatted(userInput);
        String json = aiClient.call(prompt);
        try {
            // Parse JSON thành SearchRequest
            ObjectMapper mapper = new ObjectMapper();
            SearchRequestDTO request = mapper.readValue(json, SearchRequestDTO.class);

            // Truy vấn DB
            return sanPhamRepo.timKiemTheoDieuKien(request);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý JSON hoặc truy vấn sản phẩm: " + e.getMessage(), e);
        }
    }
}
