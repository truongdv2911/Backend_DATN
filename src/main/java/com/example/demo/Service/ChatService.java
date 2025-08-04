package com.example.demo.Service;

import com.example.demo.DTOs.IntentClassificationDTO;
import com.example.demo.DTOs.SearchRequestDTO;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.ChatResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OllamaChatClient chatClient;
    private final San_pham_Repo sanPhamRepo;
    private final ObjectMapper objectMapper;

    // FAQ knowledge base
    private final Map<String, String> faqDatabase = Map.of(
            "SHIPPING", "Thời gian giao hàng: Nội thành 1-2 ngày, ngoại thành 3-5 ngày. Miễn phí ship từ 500k.",
            "WARRANTY", "Bảo hành sản phẩm LEGO: 12 tháng lỗi sản xuất, đổi trả trong 7 ngày.",
            "PAYMENT", "Thanh toán: COD, chuyển khoản, thẻ tín dụng, ví điện tử.",
            "RETURN", "Đổi trả: 7 ngày không lý do, sản phẩm nguyên vẹn, có hóa đơn.",
            "CONTACT", "Liên hệ: Hotline 1900-xxxx, Email: support@legoshop.vn"
    );

    public ChatResponse handleUserInput(String userInput) {
        try {
            // Bước 1: Phân loại intent
            IntentClassificationDTO intent = classifyIntent(userInput);

            // Bước 2: Xử lý theo intent
            switch (intent.getIntent().toUpperCase()) {
                case "SEARCH":
                    return handleProductSearch(userInput);
                case "ADVICE":
                    return handleAdviceRequest(userInput);
                case "SHIPPING":
                    return handleShippingQuery(userInput);
                case "FAQ":
                    return handleFAQQuery(userInput, intent.getExtractedInfo());
                case "GENERAL":
                default:
                    return handleGeneralChat(userInput);
            }

        } catch (Exception e) {
            return new ChatResponse("ERROR",
                    "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.", null);
        }
    }

    private IntentClassificationDTO classifyIntent(String userInput) {
        String intentPrompt = """
            Bạn là trợ lý phân tích ý định khách hàng cho cửa hàng đồ chơi LEGO.
            
            Phân tích câu sau và trả về JSON:
            {
              "intent": "SEARCH|ADVICE|SHIPPING|FAQ|GENERAL",
              "confidence": "HIGH|MEDIUM|LOW",
              "extractedInfo": "thông tin bổ sung nếu có"
            }
            
            Quy tắc phân loại:
            - SEARCH: tìm kiếm, mua sản phẩm cụ thể ("tìm lego xe hơi", "có lego nào dưới 500k")
            - ADVICE: tư vấn, gợi ý ("nên mua gì", "lego nào phù hợp", "giữa A và B chọn gì")
            - SHIPPING: giao hàng, vận chuyển ("giao hàng bao lâu", "ship có miễn phí không")
            - FAQ: câu hỏi thường gặp (bảo hành, thanh toán, đổi trả, liên hệ)
            - GENERAL: chào hỏi, cảm ơn, câu hỏi chung
            
            Câu: "%s"
            """.formatted(userInput);

        try {
            Prompt prompt = new Prompt(intentPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            return objectMapper.readValue(cleanJson, IntentClassificationDTO.class);
        } catch (Exception e) {
            // Fallback: coi như GENERAL nếu không phân loại được
            IntentClassificationDTO fallback = new IntentClassificationDTO();
            fallback.setIntent("GENERAL");
            fallback.setConfidence("LOW");
            return fallback;
        }
    }

    private ChatResponse handleProductSearch(String userInput) {
        try {
            List<SanPham> products = searchProducts(userInput);

            if (products.isEmpty()) {
                return new ChatResponse("SEARCH",
                        "Không tìm thấy sản phẩm phù hợp. Bạn có thể thử tìm kiếm với từ khóa khác hoặc liên hệ tư vấn.", null);
            }

            String message = String.format("Tìm thấy %d sản phẩm phù hợp với yêu cầu của bạn:",
                    products.size());
            return new ChatResponse("SEARCH", message, products);

        } catch (Exception e) {
            return new ChatResponse("ERROR",
                    "Lỗi tìm kiếm sản phẩm. Vui lòng thử lại.", null);
        }
    }

    private ChatResponse handleAdviceRequest(String userInput) {
        try {
            // Bước 1: Phân tích yêu cầu tư vấn để tạo tiêu chí tìm kiếm
            SearchRequestDTO searchCriteria = extractAdviceSearchCriteria(userInput);

            // Bước 2: Tìm sản phẩm phù hợp từ database
            List<SanPham> recommendedProducts = sanPhamRepo.timKiemTheoDieuKien(searchCriteria);

            // Bước 3: Nếu không tìm thấy, thử tiêu chí rộng hơn
            if (recommendedProducts.isEmpty()) {
                recommendedProducts = findAlternativeProducts(searchCriteria);
            }

            // Bước 4: Tạo lời tư vấn kèm sản phẩm
            String adviceMessage = generateAdviceWithProducts(userInput, recommendedProducts);

            return new ChatResponse("ADVICE", adviceMessage, recommendedProducts);

        } catch (Exception e) {
            return new ChatResponse("ADVICE",
                    "Để tư vấn tốt nhất, bạn vui lòng cho biết thêm: độ tuổi, sở thích, ngân sách dự kiến. " +
                            "Hoặc liên hệ hotline để được tư vấn trực tiếp.", null);
        }
    }

    private SearchRequestDTO extractAdviceSearchCriteria(String userInput) {
        String extractPrompt = """
                Bạn là chuyên gia phân tích yêu cầu tư vấn LEGO.
                            
                Từ câu tư vấn sau,Câu tư vấn: "%s" hãy trích xuất tiêu chí tìm kiếm và trả về JSON:
                {
                  "ten": null,
                  "gia": null,
                  "doTuoi": "6",
                  "xuatXu": null,
                  "thuongHieu": null,
                  "boSuuTap": null,
                  "soLuongManhGhepMin": null,
                  "danhGiaToiThieu": null
                }
                            
                Quy tắc trích xuất:
                - doTuoi: nếu có độ tuổi cụ thể ("bé 6 tuổi", "trẻ 8-12 tuổi")
                - gia: nếu có ngân sách ("dưới 500k", "từ 1-2 triệu")
                - ten/thuongHieu/boSuuTap: nếu có sở thích cụ thể ("thích xe hơi" → "xe", "thích công chúa" → "princess")
                - soLuongManhGhepMin: nếu có yêu cầu độ phức tạp ("đơn giản" → null, "phức tạp" → 500)
                            
                Ví dụ:
                - "Tư vấn lego cho bé 6 tuổi thích xe hơi ngân sách 500k" 
                  → {"doTuoi": "6", "gia": 500000, "ten": "xe"}
                - "Lego gì phù hợp trẻ 8-10 tuổi mới chơi"
                  → {"doTuoi": "8", "soLuongManhGhepMin": null}
                  
                    - "Tìm LEGO tặng sinh nhật bé gái 5 tuổi, thích công chúa" \s
                    → \s
                    {
                      "ten": "công chúa",
                      "gia": null,
                      "doTuoi": "5",
                      "xuatXu": null,
                      "thuongHieu": null,
                      "boSuuTap": "princess",
                      "soLuongManhGhepMin": null,
                      "danhGiaToiThieu": null
                    }
                                
                    - "Mình cần quà tặng cho bé trai 10 tuổi mê siêu xe, tầm giá khoảng 1 triệu" \s
                    → \s
                    {
                      "ten": "siêu xe",
                      "gia": 1000000,
                      "doTuoi": "10",
                      "xuatXu": null,
                      "thuongHieu": null,
                      "boSuuTap": null,
                      "soLuongManhGhepMin": null,
                      "danhGiaToiThieu": null
                    }
             
                """.formatted(userInput);

        try {
            Prompt prompt = new Prompt(extractPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            String processedJson = preprocessJsonNumbers(cleanJson);
            return objectMapper.readValue(processedJson, SearchRequestDTO.class);
        } catch (Exception e) {
            // Fallback: trả về criteria rỗng
            return new SearchRequestDTO();
        }
    }

    private List<SanPham> findAlternativeProducts(SearchRequestDTO originalCriteria) {
        try {
            // Thử tìm với tiêu chí rộng hơn (bỏ bớt điều kiện)
            SearchRequestDTO relaxedCriteria = new SearchRequestDTO();

            // Giữ lại những tiêu chí cơ bản nhất
            if (originalCriteria.getDoTuoi() != null) {
                relaxedCriteria.setDoTuoi(originalCriteria.getDoTuoi());
            }
            if (originalCriteria.getGia() != null) {
                relaxedCriteria.setGia(originalCriteria.getGia());
            }

            List<SanPham> products = sanPhamRepo.timKiemTheoDieuKien(relaxedCriteria);

            // Nếu vẫn không có, ưu tiên sản phẩm bán chạy (dữ liệu thực tế)
            if (products.isEmpty()) {
                List<SanPham> bestSellers = sanPhamRepo.findTopDaBan();

                // Lọc sản phẩm bán chạy theo tiêu chí cơ bản nếu có
                if (originalCriteria.getDoTuoi() != null || originalCriteria.getGia() != null) {
                    products = filterBestSellersByCriteria(bestSellers, originalCriteria);
                } else {
                    products = bestSellers;
                }

                // Nếu sau khi lọc vẫn trống, lấy tất cả sản phẩm bán chạy
                if (products.isEmpty()) {
                    products = bestSellers;
                }
            }

            // Bổ sung thêm sản phẩm bán chạy nếu kết quả ít
            if (products.size() < 5) {
                List<SanPham> additionalBestSellers = sanPhamRepo.findTopDaBan();
                products = combineAndDeduplicateProducts(products, additionalBestSellers);
            }

            // Giới hạn 8 sản phẩm để không quá dài
            return products.stream().limit(8).collect(Collectors.toList());

        } catch (Exception e) {
            // Fallback cuối: chỉ lấy sản phẩm bán chạy
            return sanPhamRepo.findTopDaBan();
        }
    }

    private List<SanPham> filterBestSellersByCriteria(List<SanPham> bestSellers, SearchRequestDTO criteria) {
        return bestSellers.stream()
                .filter(product -> {
                    // Lọc theo độ tuổi nếu có
                    if (criteria.getDoTuoi() != null && product.getDoTuoi() != null) {
                        try {
                            int requiredAge = Integer.parseInt(criteria.getDoTuoi());
                            // Lấy tuổi nhỏ nhất trong mô tả sản phẩm
                            Matcher matcher = Pattern.compile("\\d+").matcher(product.getDoTuoi().toString());
                            int productAge = matcher.find() ? Integer.parseInt(matcher.group()) : 0;
                            // Cho phép sai lệch 2 tuổi
                            if (Math.abs(requiredAge - productAge) > 2) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // Nếu không parse được thì bỏ qua điều kiện này
                        }
                    }

                    // Lọc theo giá nếu có
                    if (criteria.getGia() != null && product.getGia() != null) {
                        // Cho phép giá cao hơn 20% so với yêu cầu
                        if (product.getGia().compareTo(criteria.getGia().multiply(new BigDecimal(1.2))) > 0) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<SanPham> combineAndDeduplicateProducts(List<SanPham> existing, List<SanPham> additional) {
        Set<Integer> existingIds = existing.stream()
                .map(SanPham::getId)
                .collect(Collectors.toSet());

        List<SanPham> result = new ArrayList<>(existing);

        additional.stream()
                .filter(product -> !existingIds.contains(product.getId()))
                .forEach(result::add);

        return result;
    }

    private String generateAdviceWithProducts(String userInput, List<SanPham> products) {
        if (products.isEmpty()) {
            return "Dựa trên yêu cầu của bạn, tôi khuyên bạn nên:\n\n" +
                    "• Xem xét các bộ LEGO cơ bản phù hợp với độ tuổi\n" +
                    "• Chọn theo sở thích cá nhân (xe cộ, công chúa, siêu anh hùng...)\n" +
                    "• Bắt đầu với bộ có ít mảnh ghép để làm quen\n\n" +
                    "Vui lòng cho biết thêm thông tin để tôi tư vấn cụ thể hơn!";
        }

        // Kiểm tra xem có sản phẩm bán chạy không
        List<SanPham> bestSellers = sanPhamRepo.findTopDaBan();
        boolean hasBestSellers = products.stream()
                .anyMatch(p -> bestSellers.stream()
                        .anyMatch(bs -> bs.getId().equals(p.getId())));

        String advicePrompt = """
            Bạn là chuyên gia tư vấn LEGO với danh sách sản phẩm cụ thể.
            
            Yêu cầu tư vấn: "%s"
            
            Danh sách sản phẩm gợi ý (đã có sẵn):
            %s
            
            %s
            
            Hãy viết lời tư vấn:
            1. Mở đầu: Phân tích ngắn gọn yêu cầu của khách hàng
            2. Gợi ý: Giới thiệu 2-3 sản phẩm nổi bật nhất từ danh sách, giải thích tại sao phù hợp
            3. Ưu điểm: %s
            4. Lưu ý: Đưa ra lời khuyên bổ sung (độ tuổi, cách chơi, giá trị giáo dục...)
            
            Phong cách: Tự nhiên, thân thiện, chuyên nghiệp
            Độ dài: 150-200 từ
            Lưu ý: Không liệt kê tất cả sản phẩm, chỉ highlight những cái phù hợp nhất
            """.formatted(
                userInput,
                formatProductsWithBestSellerInfo(products, bestSellers),
                hasBestSellers ? "LƯU Ý: Một số sản phẩm trong danh sách là TOP BÁN CHẠY (được đánh dấu ⭐)" : "",
                hasBestSellers ? "Nhấn mạnh những sản phẩm bán chạy vì đây là lựa chọn được nhiều khách hàng tin tưởng" : "Tập trung vào sự phù hợp với yêu cầu"
        );

        try {
            Prompt prompt = new Prompt(advicePrompt);
            String advice = chatClient.call(prompt).getResult().getOutput().getContent();

            // Thêm thông tin về số lượng sản phẩm và điểm nhấn bán chạy
            String finalAdvice = advice.trim();

            if (hasBestSellers) {
                finalAdvice += "\n\n🔥 Một số sản phẩm gợi ý là TOP bán chạy - " +
                        "được nhiều khách hàng lựa chọn và đánh giá tích cực!";
            }

            if (products.size() > 3) {
                finalAdvice += String.format("\n\n💡 Tổng cộng có %d sản phẩm phù hợp với yêu cầu của bạn. " +
                        "Bạn có thể xem chi tiết các sản phẩm khác bên dưới!", products.size());
            }

            return finalAdvice;

        } catch (Exception e) {
            // Fallback: tạo lời tư vấn đơn giản với thông tin bán chạy
            String fallbackMessage = String.format("Dựa trên yêu cầu của bạn, tôi gợi ý %d sản phẩm LEGO phù hợp. " +
                            "Các sản phẩm này được chọn lọc kỹ càng theo tiêu chí về độ tuổi, giá cả và chất lượng.",
                    products.size());

            if (hasBestSellers) {
                fallbackMessage += " Đặc biệt, một số sản phẩm trong danh sách là TOP bán chạy, " +
                        "được nhiều gia đình tin tưởng lựa chọn!";
            }

            return fallbackMessage;
        }
    }

    private String formatProductsWithBestSellerInfo(List<SanPham> products, List<SanPham> bestSellers) {
        Set<Integer> bestSellerIds = bestSellers.stream()
                .map(SanPham::getId)
                .collect(Collectors.toSet());

        return products.stream()
                .limit(5) // Chỉ lấy 5 sản phẩm đầu để prompt không quá dài
                .map(p -> {
                    String bestSellerMark = bestSellerIds.contains(p.getId()) ? " ⭐ TOP BÁN CHẠY" : "";
                    return String.format("- %s%s | Giá: %s | Độ tuổi: %s | Mảnh ghép: %d",
                            p.getTenSanPham() != null ? p.getTenSanPham() : "N/A",
                            bestSellerMark,
                            p.getGia() != null ? String.format("%,d đ", p.getGia()) : "N/A",
                            p.getDoTuoi() != null ? p.getDoTuoi() : "N/A",
                            p.getSoLuongManhGhep() != null ? p.getSoLuongManhGhep() : 0);
                })
                .collect(Collectors.joining("\n"));
    }

    private ChatResponse handleShippingQuery(String userInput) {
        String shippingInfo = faqDatabase.get("SHIPPING");

        // Có thể tùy chỉnh thêm dựa trên câu hỏi cụ thể
        if (userInput.toLowerCase().contains("miễn phí")) {
            shippingInfo += "\n\nLưu ý: Áp dụng miễn phí ship cho đơn hàng từ 500,000đ trở lên.";
        }

        return new ChatResponse("SHIPPING", shippingInfo, null);
    }

    private ChatResponse handleFAQQuery(String userInput, String extractedInfo) {
        String lowerInput = userInput.toLowerCase();

        String response;
        if (lowerInput.contains("bảo hành")) {
            response = faqDatabase.get("WARRANTY");
        } else if (lowerInput.contains("thanh toán")) {
            response = faqDatabase.get("PAYMENT");
        } else if (lowerInput.contains("đổi") || lowerInput.contains("trả")) {
            response = faqDatabase.get("RETURN");
        } else if (lowerInput.contains("liên hệ")) {
            response = faqDatabase.get("CONTACT");
        } else {
            response = "Câu hỏi thường gặp:\n\n" +
                    "🚚 " + faqDatabase.get("SHIPPING") + "\n\n" +
                    "🛡️ " + faqDatabase.get("WARRANTY") + "\n\n" +
                    "💳 " + faqDatabase.get("PAYMENT") + "\n\n" +
                    "🔄 " + faqDatabase.get("RETURN") + "\n\n" +
                    "📞 " + faqDatabase.get("CONTACT");
        }

        return new ChatResponse("FAQ", response, null);
    }

    private ChatResponse handleGeneralChat(String userInput) {
        String generalPrompt = """
            Bạn là nhân viên tư vấn thân thiện của cửa hàng đồ chơi LEGO.
            
            Trả lời khách hàng một cách tự nhiên, thân thiện cho câu: "%s"
            
            Nếu có thể, hãy hướng dẫn khách hàng đến các dịch vụ:
            - Tìm kiếm sản phẩm
            - Tư vấn mua hàng
            - Thông tin giao hàng
            - Chính sách bảo hành/đổi trả
            
            Trả lời ngắn gọn (không quá 100 từ).
            """.formatted(userInput);

        try {
            Prompt prompt = new Prompt(generalPrompt);
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            return new ChatResponse("GENERAL", response.trim(), null);
        } catch (Exception e) {
            return new ChatResponse("GENERAL",
                    "Xin chào! Tôi có thể giúp bạn tìm kiếm sản phẩm LEGO, tư vấn mua hàng, " +
                            "hoặc trả lời các câu hỏi về giao hàng, bảo hành. Bạn cần hỗ trợ gì ạ?", null);
        }
    }

    // Phương thức tìm kiếm sản phẩm gốc (đã có)
    private List<SanPham> searchProducts(String userInput) {
        String userPrompt = """
                Bạn là trợ lý thông minh chuyên giúp khách hàng tìm đồ chơi LEGO.
                            
                Hãy phân tích câu sau và trả về JSON như sau:
                {
                  "ten": "lego sieu xe",
                  "gia": 1000000,
                  "doTuoi": "6",
                  "xuatXu": "Đan Mạch",
                  "thuongHieu": "LEGO SPEED CHAMPIONS",
                  "boSuuTap": "LEGO SPEED CHAMPIONS ALL",
                  "soLuongManhGhepMin": 500,
                  "danhGiaToiThieu": "5 sao"
                }
                            
                QUAN TRỌNG:
                - soLuongManhGhepMin phải là số nguyên (ví dụ: 500, 1000), KHÔNG được là string như ">1000"
                - gia phải là số nguyên
                - Nếu có điều kiện như "trên 1000 mảnh", hãy đặt soLuongManhGhepMin = 1000
                - Nếu có điều kiện như "dưới 500 mảnh", hãy đặt soLuongManhGhepMin = null
                - Các trường không có thông tin thì để null
                - Chỉ trả lại đúng JSON, không giải thích thêm
                            
                    ### Một số ví dụ:
                                
                    Câu: "Tìm LEGO siêu xe cho bé 6 tuổi, khoảng 1 triệu, từ Đan Mạch"
                    →
                    {
                      "ten": "LEGO siêu xe",
                      "gia": 1000000,
                      "doTuoi": "6",
                      "xuatXu": "Đan Mạch",
                      "thuongHieu": null,
                      "boSuuTap": null,
                      "soLuongManhGhepMin": null,
                      "danhGiaToiThieu": null
                    }
                                
                    Câu: "Mình muốn bộ LEGO từ thương hiệu LEGO Technic, hơn 1000 mảnh, giá dưới 2 triệu"
                    →
                    {
                      "ten": null,
                      "gia": 2000000,
                      "doTuoi": null,
                      "xuatXu": null,
                      "thuongHieu": "LEGO Technic",
                      "boSuuTap": null,
                      "soLuongManhGhepMin": 1000,
                      "danhGiaToiThieu": null
                    }
                                
                    Câu: "Cho mình LEGO chủ đề Star Wars khoảng 800k, đánh giá 5 sao, trên 500 mảnh"
                    →
                    {
                      "ten": "LEGO chủ đề Star Wars",
                      "gia": 800000,
                      "doTuoi": null,
                      "xuatXu": null,
                      "thuongHieu": null,
                      "boSuuTap": "Star Wars",
                      "soLuongManhGhepMin": 500,
                      "danhGiaToiThieu": "5 sao"
                    }
                Câu: "%s"
                """.formatted(userInput);

        try {
            Prompt prompt = new Prompt(userPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            String processedJson = preprocessJsonNumbers(cleanJson);
            SearchRequestDTO request = objectMapper.readValue(processedJson, SearchRequestDTO.class);
            return sanPhamRepo.timKiemTheoDieuKien(request);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tìm kiếm sản phẩm: " + e.getMessage(), e);
        }
    }

    // Các phương thức utility gốc
    private String cleanJsonResponse(String jsonResponse) {
        String cleaned = jsonResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String preprocessJsonNumbers(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("soLuongManhGhepMin") && node.get("soLuongManhGhepMin").isTextual()) {
                String value = node.get("soLuongManhGhepMin").asText();
                Integer numericValue = extractNumericValue(value);
                return json.replaceFirst(
                        "\"soLuongManhGhepMin\"\\s*:\\s*\"[^\"]*\"",
                        "\"soLuongManhGhepMin\": " + (numericValue != null ? numericValue : "null")
                );
            }
            return json;
        } catch (Exception e) {
            return json;
        }
    }

    private Integer extractNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String numStr = value.replaceAll("[^0-9]", "");
        if (!numStr.isEmpty()) {
            try {
                return Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
