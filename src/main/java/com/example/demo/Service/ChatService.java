package com.example.demo.Service;

import com.example.demo.DTOs.IntentClassificationDTO;
import com.example.demo.DTOs.SearchRequestDTO;
import com.example.demo.Entity.AnhSp;
import com.example.demo.Entity.ChatMemory;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.Anh_sp_Repo;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.AnhResponse;
import com.example.demo.Responses.ChatResponse;
import com.example.demo.Responses.SanPhamResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OllamaChatClient chatClient;
    private final San_pham_Repo sanPhamRepo;
    private final ObjectMapper objectMapper;
    private final Anh_sp_Repo anhSpRepo;

    // Chat memory storage - sử dụng ConcurrentHashMap để thread-safe
    private final Map<String, List<ChatMemory>> userChatMemory = new ConcurrentHashMap<>();
    private static final int MAX_MEMORY_SIZE = 10; // Giới hạn 10 tin nhắn gần nhất

    // FAQ knowledge base - cải thiện với thông tin chi tiết hơn
    private final Map<String, String> faqDatabase = Map.of(
            "SHIPPING", "🚚 **Thông tin giao hàng:**\n• Nội thành Hà Nội, TP.HCM: 1-2 ngày làm việc\n• Ngoại thành: 3-5 ngày làm việc\n• Miễn phí ship cho đơn hàng từ 500,000đ\n• Phí ship: 30,000đ cho đơn dưới 500,000đ",
            "WARRANTY", "🛡️ **Chính sách bảo hành:**\n• Bảo hành 12 tháng lỗi sản xuất\n• Đổi trả miễn phí trong 7 ngày đầu\n• Sản phẩm phải còn nguyên vẹn, đầy đủ phụ kiện\n• Hỗ trợ bảo hành tại tất cả chi nhánh",
            "PAYMENT", "💳 **Phương thức thanh toán:**\n• Thanh toán khi nhận hàng (COD)\n• Chuyển khoản ngân hàng\n• Thẻ tín dụng/ghi nợ\n• Ví điện tử (MoMo, ZaloPay, VNPay)\n• Trả góp 0% lãi suất (3-12 tháng)",
            "RETURN", "🔄 **Chính sách đổi trả:**\n• Đổi trả không lý do trong 7 ngày\n• Sản phẩm phải nguyên vẹn, chưa sử dụng\n• Có hóa đơn mua hàng\n• Miễn phí đổi trả nếu lỗi từ cửa hàng",
            "CONTACT", "📞 **Thông tin liên hệ:**\n• Hotline: 1900-xxxx (8:00-22:00)\n• Email: support@legoshop.vn\n• Fanpage: facebook.com/legoshopvn\n• Zalo: zalo.me/legoshop\n• Chi nhánh: 123 ABC Street, Hà Nội"
    );

    // Product categories for better advice
    private final Map<String, List<String>> productCategories = Map.of(
            "TECHNIC", List.of("LEGO TECHNIC", "MECHANICAL", "ENGINEERING"),
            "CITY", List.of("LEGO CITY", "POLICE", "FIRE", "CONSTRUCTION"),
            "CREATOR", List.of("LEGO CREATOR", "EXPERT", "ADVANCED"),
            "STAR_WARS", List.of("LEGO STAR WARS", "STAR WARS"),
            "FRIENDS", List.of("LEGO FRIENDS", "FRIENDS"),
            "DUPLO", List.of("LEGO DUPLO", "DUPLO"),
            "JUNIORS", List.of("LEGO JUNIORS", "JUNIORS")
    );

    /**
     * Main method xử lý input từ user với memory context
     */
    public ChatResponse handleUserInput(String userInput, String sessionId) {
        try {
            // Lấy chat history để có context
            List<ChatMemory> chatHistory = getChatHistory(sessionId);

            // Phân loại intent với context cải thiện
            IntentClassificationDTO intent = classifyIntentWithContext(userInput, chatHistory);

            // Xử lý theo intent
            ChatResponse response = processIntentRequest(intent, userInput, chatHistory);

            // Lưu vào memory
            saveChatMemory(sessionId, userInput, response.getMessage(), intent.getIntent());

            return response;

        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse("ERROR",
                    "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau hoặc liên hệ hotline để được hỗ trợ.", null);
            saveChatMemory(sessionId, userInput, errorResponse.getMessage(), "ERROR");
            return errorResponse;
        }
    }

    /**
     * Phân loại intent với context từ chat history - CẢI THIỆN
     */
    private IntentClassificationDTO classifyIntentWithContext(String userInput, List<ChatMemory> chatHistory) {
        String contextInfo = buildContextFromHistory(chatHistory);

        String intentPrompt = String.format("""
            Bạn là AI chuyên phân tích ý định khách hàng cho cửa hàng LEGO.
            
            **NGỮ CẢNH CUỘC TRÒ CHUYỆN:**
            %s
            
            **PHÂN TÍCH CÂU:** "%s"
            
            **QUY TẮC PHÂN LOẠI (CHỌN 1 INTENT DUY NHẤT):**
            1. SEARCH: Tìm kiếm sản phẩm cụ thể (tên, loại, giá, thương hiệu)
            2. ADVICE: Tư vấn, gợi ý, hỏi ý kiến về sản phẩm (ưu tiên cho "tư vấn", "bán chạy", "phổ biến")
            3. SHIPPING: Giao hàng, vận chuyển, thời gian ship
            4. FAQ: Bảo hành, thanh toán, đổi trả, liên hệ
            5. FOLLOW_UP: Câu hỏi tiếp theo về cuộc trò chuyện trước
            6. GENERAL: Chào hỏi, cảm ơn, câu hỏi chung
            
            **LƯU Ý:** Chỉ trả về 1 intent duy nhất, không dùng dấu |
            
            **TRẢ VỀ JSON:**
            {
              "intent": "SEARCH|ADVICE|SHIPPING|FAQ|FOLLOW_UP|GENERAL",
              "confidence": "HIGH|MEDIUM|LOW",
              "extractedInfo": "thông tin quan trọng từ câu"
            }
            """, contextInfo, userInput);

        return executeIntentClassification(intentPrompt);
    }

    /**
     * Xử lý request theo intent - CẢI THIỆN
     */
    private ChatResponse processIntentRequest(IntentClassificationDTO intent, String userInput, List<ChatMemory> chatHistory) {
        System.out.println("DEBUG: Processing intent: " + intent.getIntent() + " for input: " + userInput);
        
        return switch (intent.getIntent().toUpperCase()) {
            case "SEARCH" -> {
                System.out.println("DEBUG: Routing to SEARCH handler");
                yield handleProductSearch(userInput, chatHistory);
            }
            case "ADVICE" -> {
                System.out.println("DEBUG: Routing to ADVICE handler");
                yield handleAdviceRequest(userInput, chatHistory);
            }
            case "SHIPPING" -> {
                System.out.println("DEBUG: Routing to SHIPPING handler");
                yield handleShippingQuery(userInput);
            }
            case "FAQ" -> {
                System.out.println("DEBUG: Routing to FAQ handler");
                yield handleFAQQuery(userInput, intent.getExtractedInfo());
            }
            case "FOLLOW_UP" -> {
                System.out.println("DEBUG: Routing to FOLLOW_UP handler");
                yield handleFollowUpQuestion(userInput, chatHistory);
            }
            case "GENERAL" -> {
                System.out.println("DEBUG: Routing to GENERAL handler");
                yield handleGeneralChat(userInput, chatHistory);
            }
            default -> {
                System.out.println("DEBUG: Routing to default GENERAL handler");
                yield handleGeneralChat(userInput, chatHistory);
            }
        };
    }

    /**
     * Xử lý tìm kiếm sản phẩm - CẢI THIỆN
     */
    private ChatResponse handleProductSearch(String userInput, List<ChatMemory> chatHistory) {
        try {
            // Tìm kiếm với context
            List<SanPham> products = searchProductsWithContext(userInput, chatHistory);

            if (products.isEmpty()) {
                // Fallback: tìm kiếm rộng hơn
                products = searchProductsFallback(userInput);
                
                if (products.isEmpty()) {
                    return new ChatResponse("SEARCH",
                            "Không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. " +
                            "Bạn có thể:\n• Thử tìm kiếm với từ khóa khác\n• Cho tôi biết thêm thông tin (độ tuổi, ngân sách, sở thích)\n• Liên hệ hotline để được tư vấn trực tiếp", null);
                }
            }

            // Convert to DTO
            List<SanPhamResponseDTO> productDTOs = products.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            String message = generateSearchResponse(userInput, products, chatHistory);
            return new ChatResponse("SEARCH", message, productDTOs);

        } catch (Exception e) {
            return new ChatResponse("ERROR", "Lỗi tìm kiếm sản phẩm. Vui lòng thử lại hoặc liên hệ hỗ trợ.", null);
        }
    }

    /**
     * Xử lý tư vấn với context - CẢI THIỆN MẠNH MẼ
     */
    private ChatResponse handleAdviceRequest(String userInput, List<ChatMemory> chatHistory) {
        try {
            System.out.println("DEBUG: Entering handleAdviceRequest with: " + userInput);
            String lowerInput = userInput.toLowerCase();
            System.out.println("DEBUG: Lower input: " + lowerInput);
            
            // Kiểm tra nếu yêu cầu về sản phẩm bán chạy
            if (lowerInput.contains("bán chạy") || lowerInput.contains("phổ biến") || 
                lowerInput.contains("nổi tiếng") || lowerInput.contains("hot") ||
                lowerInput.contains("best") || lowerInput.contains("top") ||
                lowerInput.contains("nhất")) {
                
                System.out.println("DEBUG: Detected best-selling request, processing...");
                
                try {
                    List<SanPham> bestSellingProducts = findBestSellingProducts();
                    System.out.println("Found " + bestSellingProducts.size() + " best selling products");
                    
                    if (bestSellingProducts.isEmpty()) {
                        // Fallback: lấy tất cả sản phẩm
                        bestSellingProducts = sanPhamRepo.findAll().stream()
                                .limit(10)
                                .collect(Collectors.toList());
                        System.out.println("Fallback: Found " + bestSellingProducts.size() + " products");
                    }
                    
                    // Convert to DTO
                    List<SanPhamResponseDTO> bestSellingDTOs = bestSellingProducts.stream()
                            .map(this::convertToResponseDTO)
                            .collect(Collectors.toList());
                    
                    String adviceMessage = generateBestSellingAdvice(userInput, bestSellingProducts);
                    return new ChatResponse("ADVICE", adviceMessage, bestSellingDTOs);
                    
                } catch (Exception e) {
                    System.err.println("Error finding best selling products: " + e.getMessage());
                    // Fallback: lấy tất cả sản phẩm
                    List<SanPham> allProducts = sanPhamRepo.findAll().stream()
                            .limit(10)
                            .collect(Collectors.toList());
                    
                    // Convert to DTO
                    List<SanPhamResponseDTO> allProductDTOs = allProducts.stream()
                            .map(this::convertToResponseDTO)
                            .collect(Collectors.toList());
                    
                    String fallbackMessage = "🔥 **TOP SẢN PHẨM LEGO PHỔ BIẾN** 🔥\n\n" +
                            "Dựa trên yêu cầu của bạn, đây là những sản phẩm LEGO được nhiều khách hàng yêu thích:\n\n" +
                            formatProductsForAdvice(allProducts) + "\n\n" +
                            "💡 **Lời khuyên:** Những sản phẩm này đều có chất lượng cao và phù hợp với nhiều độ tuổi. " +
                            "Bạn có thể chọn theo sở thích hoặc liên hệ tôi để được tư vấn chi tiết hơn!";
                    
                    return new ChatResponse("ADVICE", fallbackMessage, allProductDTOs);
                }
            }
            
            // Phân tích nhu cầu người dùng cho các trường hợp khác
            UserNeeds userNeeds = analyzeUserNeeds(userInput, chatHistory);
            
            // Tìm sản phẩm phù hợp
            List<SanPham> recommendedProducts = findProductsByNeeds(userNeeds);
            
            // Convert to DTO
            List<SanPhamResponseDTO> recommendedDTOs = recommendedProducts.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            
            // Tạo lời tư vấn thông minh
            String adviceMessage = generateIntelligentAdvice(userInput, recommendedProducts, userNeeds, chatHistory);
            
            return new ChatResponse("ADVICE", adviceMessage, recommendedDTOs);

        } catch (Exception e) {
            System.err.println("Error in handleAdviceRequest: " + e.getMessage());
            e.printStackTrace();
            return new ChatResponse("ADVICE",
                    "Để tư vấn tốt nhất, bạn vui lòng cho biết:\n" +
                    "• Độ tuổi người chơi\n" +
                    "• Sở thích (xe hơi, robot, thành phố, v.v.)\n" +
                    "• Ngân sách dự kiến\n" +
                    "• Kinh nghiệm chơi LEGO\n\n" +
                    "Hoặc liên hệ hotline để được tư vấn trực tiếp!", null);
        }
    }

    /**
     * Xử lý câu hỏi follow-up - CẢI THIỆN
     */
    private ChatResponse handleFollowUpQuestion(String userInput, List<ChatMemory> chatHistory) {
        try {
            // Lấy context từ câu hỏi trước đó
            String contextInfo = buildContextFromHistory(chatHistory);
            
            // Phân tích follow-up question
            FollowUpAnalysis analysis = analyzeFollowUpQuestion(userInput, chatHistory);

            String followUpPrompt = String.format("""
                Bạn là chuyên gia tư vấn LEGO thân thiện và chuyên nghiệp.
                
                **NGỮ CẢNH CUỘC TRÒ CHUYỆN:**
                %s
                
                **CÂU HỎI FOLLOW-UP:** "%s"
                
                **PHÂN TÍCH:** %s
                
                Hãy trả lời một cách tự nhiên, thân thiện và hữu ích.
                Nếu cần tìm kiếm sản phẩm, hãy đề xuất cụ thể.
                Trả lời ngắn gọn nhưng đầy đủ thông tin.
                """, contextInfo, userInput, analysis.getAnalysis());

            Prompt prompt = new Prompt(followUpPrompt);
            String response = chatClient.call(prompt).getResult().getOutput().getContent();

            // Convert products to DTO
            List<SanPhamResponseDTO> productDTOs = analysis.getProducts().stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            return new ChatResponse("FOLLOW_UP", response.trim(), productDTOs);

        } catch (Exception e) {
            return new ChatResponse("FOLLOW_UP",
                    "Tôi hiểu bạn đang hỏi thêm về cuộc trò chuyện trước. " +
                    "Bạn có thể nói rõ hơn để tôi hỗ trợ tốt hơn không?", null);
        }
    }

    /**
     * Xử lý shipping query - CẢI THIỆN
     */
    private ChatResponse handleShippingQuery(String userInput) {
        String shippingInfo = faqDatabase.get("SHIPPING");
        String lowerInput = userInput.toLowerCase();

        if (lowerInput.contains("miễn phí") || lowerInput.contains("free")) {
            shippingInfo += "\n\n💡 **Lưu ý:** Áp dụng miễn phí ship cho đơn hàng từ 500,000đ trở lên!";
        }
        
        if (lowerInput.contains("thời gian") || lowerInput.contains("bao lâu")) {
            shippingInfo += "\n\n⏰ **Thời gian giao hàng:**\n• Giao trong ngày: Đặt trước 14:00\n• Giao nhanh: +50,000đ (giao trong 2-4 giờ)";
        }

        return new ChatResponse("SHIPPING", shippingInfo, null);
    }

    /**
     * Xử lý FAQ query - CẢI THIỆN
     */
    private ChatResponse handleFAQQuery(String userInput, String extractedInfo) {
        String lowerInput = userInput.toLowerCase();

        String response = determineFAQResponse(lowerInput);
        return new ChatResponse("FAQ", response, null);
    }

    /**
     * Xử lý chat chung với context - CẢI THIỆN
     */
    private ChatResponse handleGeneralChat(String userInput, List<ChatMemory> chatHistory) {
        String contextInfo = buildContextFromHistory(chatHistory);

        String generalPrompt = String.format("""
            Bạn là nhân viên tư vấn thân thiện và chuyên nghiệp của cửa hàng LEGO.
            
            **NGỮ CẢNH:** %s
            
            **CÂU KHÁCH HÀNG:** "%s"
            
            Hãy trả lời một cách tự nhiên, thân thiện và hữu ích.
            Nếu có thể, hãy hướng dẫn khách hàng đến các dịch vụ phù hợp.
            Trả lời ngắn gọn (50-100 từ) nhưng đầy đủ thông tin.
            Sử dụng emoji để tạo cảm giác thân thiện.
            """, contextInfo, userInput);

        try {
            Prompt prompt = new Prompt(generalPrompt);
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            return new ChatResponse("GENERAL", response.trim(), null);
        } catch (Exception e) {
            return new ChatResponse("GENERAL",
                    "👋 Xin chào! Tôi có thể giúp bạn:\n" +
                    "🔍 Tìm kiếm sản phẩm LEGO\n" +
                    "💡 Tư vấn mua hàng\n" +
                    "🚚 Thông tin giao hàng\n" +
                    "🛡️ Chính sách bảo hành\n\n" +
                    "Bạn cần hỗ trợ gì ạ?", null);
        }
    }

    /**
     * Memory Management Methods
     */
    private List<ChatMemory> getChatHistory(String sessionId) {
        return userChatMemory.getOrDefault(sessionId, new ArrayList<>());
    }

    private void saveChatMemory(String sessionId, String userInput, String botResponse, String intent) {
        List<ChatMemory> history = userChatMemory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        history.add(new ChatMemory(userInput, botResponse, intent));

        // Giới hạn kích thước memory
        if (history.size() > MAX_MEMORY_SIZE) {
            history.remove(0); // Xóa tin nhắn cũ nhất
        }
    }

    private String buildContextFromHistory(List<ChatMemory> chatHistory) {
        if (chatHistory.isEmpty()) {
            return "Đây là đầu cuộc trò chuyện.";
        }

        return chatHistory.stream()
                .limit(5) // Chỉ lấy 5 tin nhắn gần nhất
                .map(memory -> String.format("User: %s | Bot: %s",
                        memory.getUserInput(),
                        memory.getBotResponse().substring(0, Math.min(100, memory.getBotResponse().length()))))
                .collect(Collectors.joining("\n"));
    }

    public void clearChatMemory(String sessionId) {
        userChatMemory.remove(sessionId);
    }

    public int getChatMemorySize(String sessionId) {
        return userChatMemory.getOrDefault(sessionId, new ArrayList<>()).size();
    }

    /**
     * Helper Methods - CẢI THIỆN
     */
    private IntentClassificationDTO executeIntentClassification(String intentPrompt) {
        try {
            Prompt prompt = new Prompt(intentPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            
            System.out.println("DEBUG: AI Response: " + jsonResponse);
            System.out.println("DEBUG: Cleaned JSON: " + cleanJson);
            
            IntentClassificationDTO result = objectMapper.readValue(cleanJson, IntentClassificationDTO.class);
            System.out.println("DEBUG: Parsed Intent: " + result.getIntent() + ", Confidence: " + result.getConfidence());
            
            // Fix: Handle multiple intents separated by |
            String intent = result.getIntent();
            if (intent != null && intent.contains("|")) {
                String[] intents = intent.split("\\|");
                // Prioritize ADVICE over SEARCH
                if (intents.length > 0) {
                    for (String singleIntent : intents) {
                        if ("ADVICE".equalsIgnoreCase(singleIntent.trim())) {
                            intent = "ADVICE";
                            break;
                        }
                    }
                    // If no ADVICE found, take the first one
                    if (intent.contains("|")) {
                        intent = intents[0].trim();
                    }
                }
                result.setIntent(intent);
                System.out.println("DEBUG: Fixed Intent: " + intent);
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("DEBUG: Error in AI classification: " + e.getMessage());
            // Fallback logic cải thiện
            IntentClassificationDTO fallback = new IntentClassificationDTO();
            fallback.setIntent(determineIntentFallback(intentPrompt));
            fallback.setConfidence("LOW");
            System.out.println("DEBUG: Using fallback intent: " + fallback.getIntent());
            return fallback;
        }
    }

    private String determineIntentFallback(String userInput) {
        String lowerInput = userInput.toLowerCase();
        
        System.out.println("DEBUG: Analyzing intent for: " + userInput);
        System.out.println("DEBUG: Lower input: " + lowerInput);
        
        // ADVICE - ưu tiên cao nhất cho tư vấn
        if (lowerInput.contains("tư vấn") || lowerInput.contains("gợi ý") || lowerInput.contains("khuyên") ||
            lowerInput.contains("bán chạy") || lowerInput.contains("phổ biến") || lowerInput.contains("nổi tiếng") ||
            lowerInput.contains("đề xuất") || lowerInput.contains("giới thiệu") || lowerInput.contains("cho tôi") ||
            lowerInput.contains("nhất")) {
            System.out.println("DEBUG: Detected ADVICE intent");
            return "ADVICE";
        }
        
        // SEARCH - tìm kiếm sản phẩm cụ thể
        if (lowerInput.contains("tìm") || lowerInput.contains("mua") || lowerInput.contains("có") || 
            lowerInput.contains("lego") || lowerInput.contains("xe") || lowerInput.contains("robot") ||
            lowerInput.contains("sản phẩm") || lowerInput.contains("đồ chơi")) {
            System.out.println("DEBUG: Detected SEARCH intent");
            return "SEARCH";
        }
        
        // SHIPPING - giao hàng
        if (lowerInput.contains("giao") || lowerInput.contains("ship") || lowerInput.contains("vận chuyển") ||
            lowerInput.contains("thời gian") || lowerInput.contains("bao lâu") || lowerInput.contains("miễn phí")) {
            System.out.println("DEBUG: Detected SHIPPING intent");
            return "SHIPPING";
        }
        
        // FAQ - câu hỏi thường gặp
        if (lowerInput.contains("bảo hành") || lowerInput.contains("thanh toán") || lowerInput.contains("đổi trả") ||
            lowerInput.contains("liên hệ") || lowerInput.contains("hotline") || lowerInput.contains("chính sách")) {
            System.out.println("DEBUG: Detected FAQ intent");
            return "FAQ";
        }
        
        System.out.println("DEBUG: Defaulting to GENERAL intent");
        return "GENERAL";
    }

    private List<SanPham> searchProductsWithContext(String userInput, List<ChatMemory> chatHistory) {
        String contextInfo = buildContextFromHistory(chatHistory);
        
        String searchPrompt = String.format("""
            Bạn là chuyên gia phân tích tìm kiếm LEGO.
            
            **NGỮ CẢNH:** %s
            **CÂU TÌM KIẾM:** "%s"
            
            Trả về JSON với thông tin tìm kiếm:
            {
              "ten": "tên sản phẩm cụ thể",
              "gia": null,
              "doTuoi": "độ tuổi",
              "xuatXu": null,
              "thuongHieu": "thương hiệu LEGO",
              "boSuuTap": "bộ sưu tập",
              "soLuongManhGhepMin": null,
              "danhGiaToiThieu": null
            }
            
            Chỉ điền thông tin chắc chắn, để null nếu không rõ.
            """, contextInfo, userInput);

        try {
            Prompt prompt = new Prompt(searchPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            String processedJson = preprocessJsonNumbers(cleanJson);
            SearchRequestDTO request = objectMapper.readValue(processedJson, SearchRequestDTO.class);
            return sanPhamRepo.timKiemTheoDieuKien(request);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tìm kiếm sản phẩm: " + e.getMessage(), e);
        }
    }

    private List<SanPham> searchProductsFallback(String userInput) {
        // Fallback: tìm kiếm theo từ khóa đơn giản
        try {
            String[] keywords = userInput.toLowerCase().split("\\s+");
            List<SanPham> allProducts = sanPhamRepo.findAll();
            
            return allProducts.stream()
                    .filter(product -> {
                        String productName = product.getTenSanPham() != null ? 
                            product.getTenSanPham().toLowerCase() : "";
                        return keywords.length > 0 && 
                               keywords[0].length() > 2 && 
                               productName.contains(keywords[0]);
                    })
                    .limit(10)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String generateSearchResponse(String userInput, List<SanPham> products, List<ChatMemory> chatHistory) {
        if (products.size() == 1) {
            SanPham product = products.get(0);
            return String.format("🎯 Tìm thấy sản phẩm phù hợp:\n\n" +
                    "**%s**\n" +
                    "💰 Giá: %s\n" +
                    "👶 Độ tuổi: %s\n" +
                    "🏷️ Thương hiệu: %s\n\n" +
                    "Bạn có muốn tôi tư vấn thêm về sản phẩm này không?",
                    product.getTenSanPham(),
                    product.getGia() != null ? String.format("%,.0f đ", product.getGia()) : "N/A",
                    product.getDoTuoi(),
                    product.getThuongHieu());
        } else {
            return String.format("🔍 Tìm thấy %d sản phẩm phù hợp với yêu cầu của bạn:\n\n" +
                    "Bạn có thể cho tôi biết thêm thông tin để tôi tư vấn cụ thể hơn không?", products.size());
        }
    }

    // Inner classes for better organization
    private static class UserNeeds {
        private String ageGroup;
        private String interests;
        private BigDecimal budget;
        private String experience;
        private String category;
        
        // Getters and setters
        public String getAgeGroup() { return ageGroup; }
        public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
        public String getInterests() { return interests; }
        public void setInterests(String interests) { this.interests = interests; }
        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal budget) { this.budget = budget; }
        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    private static class FollowUpAnalysis {
        private String analysis;
        private List<SanPham> products;
        
        public FollowUpAnalysis(String analysis, List<SanPham> products) {
            this.analysis = analysis;
            this.products = products;
        }
        
        public String getAnalysis() { return analysis; }
        public List<SanPham> getProducts() { return products; }
    }

    private UserNeeds analyzeUserNeeds(String userInput, List<ChatMemory> chatHistory) {
        String contextInfo = buildContextFromHistory(chatHistory);
        
        String analysisPrompt = String.format("""
            Phân tích nhu cầu người dùng từ câu tư vấn và ngữ cảnh.
            
            **NGỮ CẢNH:** %s
            **CÂU TƯ VẤN:** "%s"
            
            Trả về JSON:
            {
              "ageGroup": "độ tuổi (trẻ em/thanh thiếu niên/người lớn)",
              "interests": "sở thích (xe hơi/robot/thành phố/v.v.)",
              "budget": null,
              "experience": "kinh nghiệm (mới bắt đầu/trung bình/nâng cao)",
              "category": "danh mục LEGO phù hợp"
            }
            """, contextInfo, userInput);

        try {
            Prompt prompt = new Prompt(analysisPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            return objectMapper.readValue(cleanJson, UserNeeds.class);
        } catch (Exception e) {
            UserNeeds needs = new UserNeeds();
            needs.setAgeGroup("trẻ em");
            needs.setExperience("mới bắt đầu");
            return needs;
        }
    }

    private List<SanPham> findProductsByNeeds(UserNeeds needs) {
        try {
            SearchRequestDTO criteria = new SearchRequestDTO();
            
            // Set criteria based on needs
            if (needs.getAgeGroup() != null) {
                criteria.setDoTuoi(extractAgeFromGroup(needs.getAgeGroup()));
            }
            if (needs.getCategory() != null) {
                criteria.setThuongHieu(needs.getCategory());
            }
            
            List<SanPham> products = sanPhamRepo.timKiemTheoDieuKien(criteria);
            
            // If no products found, try alternative search
            if (products.isEmpty()) {
                products = findAlternativeProducts(criteria);
            }
            
            return products.stream().limit(6).collect(Collectors.toList());
            
        } catch (Exception e) {
            return findAlternativeProducts(new SearchRequestDTO());
        }
    }

    /**
     * Tìm sản phẩm bán chạy nhất
     */
    private List<SanPham> findBestSellingProducts() {
        try {
            // Thử lấy dữ liệu bán chạy từ 6 tháng gần nhất (rộng hơn)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(6);
            
            System.out.println("Searching for best selling products from " + startDate + " to " + endDate);
            
            List<Object[]> results = sanPhamRepo.findTopDaBan(startDate, endDate);
            System.out.println("Found " + results.size() + " results from database");
            
            if (results.isEmpty()) {
                System.out.println("No best selling data found, using fallback");
                return getFallbackProducts();
            }
            
            List<String> tenSP = results.stream()
                    .map(r -> (String) r[1])
                    .limit(10) // Lấy top 10 sản phẩm bán chạy
                    .toList();
            
            System.out.println("Product names: " + tenSP);
            
            List<SanPham> products = tenSP.stream()
                    .map(sanPhamRepo::findByTenSanPham)
                    .filter(product -> product != null)
                    .collect(Collectors.toList());
            
            System.out.println("Found " + products.size() + " valid products");
            
            // Nếu không tìm thấy sản phẩm nào, dùng fallback
            if (products.isEmpty()) {
                System.out.println("No valid products found, using fallback");
                return getFallbackProducts();
            }
            
            return products;
                    
        } catch (Exception e) {
            System.err.println("Error in findBestSellingProducts: " + e.getMessage());
            e.printStackTrace();
            return getFallbackProducts();
        }
    }
    
    /**
     * Fallback: lấy sản phẩm phổ biến
     */
    private List<SanPham> getFallbackProducts() {
        try {
            // Lấy tất cả sản phẩm và sắp xếp theo giá trị phổ biến
            List<SanPham> allProducts = sanPhamRepo.findAll();
            
            // Sắp xếp theo đánh giá trung bình và số lượng vote
            List<SanPham> sortedProducts = allProducts.stream()
                    .filter(p -> p.getDanhGiaTrungBinh() != null && p.getDanhGiaTrungBinh() > 0)
                    .sorted((p1, p2) -> {
                        // Ưu tiên đánh giá cao và nhiều vote
                        double score1 = p1.getDanhGiaTrungBinh() * (p1.getSoLuongVote() != null ? p1.getSoLuongVote() : 1);
                        double score2 = p2.getDanhGiaTrungBinh() * (p2.getSoLuongVote() != null ? p2.getSoLuongVote() : 1);
                        return Double.compare(score2, score1); // Giảm dần
                    })
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Nếu không có sản phẩm có đánh giá, lấy 10 sản phẩm đầu tiên
            if (sortedProducts.isEmpty()) {
                sortedProducts = allProducts.stream()
                        .limit(10)
                        .collect(Collectors.toList());
            }
            
            System.out.println("Fallback: Found " + sortedProducts.size() + " products");
            return sortedProducts;
            
        } catch (Exception e) {
            System.err.println("Error in getFallbackProducts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String extractAgeFromGroup(String ageGroup) {
        if (ageGroup.contains("trẻ em") || ageGroup.contains("nhỏ")) {
            return "4-6";
        } else if (ageGroup.contains("thiếu niên")) {
            return "7-12";
        } else if (ageGroup.contains("người lớn")) {
            return "18+";
        }
        return "6+";
    }

    private String generateIntelligentAdvice(String userInput, List<SanPham> products, UserNeeds needs, List<ChatMemory> chatHistory) {
        if (products.isEmpty()) {
            return "Dựa trên yêu cầu của bạn, tôi khuyên bạn nên xem xét các bộ LEGO cơ bản phù hợp với độ tuổi. " +
                    "Vui lòng cho biết thêm thông tin để tôi tư vấn cụ thể hơn!";
        }

        String contextInfo = buildContextFromHistory(chatHistory);

        String advicePrompt = String.format("""
            Bạn là chuyên gia tư vấn LEGO chuyên nghiệp.
            
            **NGỮ CẢNH:** %s
            **YÊU CẦU:** "%s"
            **PHÂN TÍCH NHU CẦU:** %s
            **SẢN PHẨM GỢI Ý:** %s
            
            Hãy viết lời tư vấn chuyên nghiệp, thân thiện (150-200 từ):
            - Giải thích tại sao chọn những sản phẩm này
            - Đưa ra lời khuyên cụ thể
            - Hướng dẫn cách chọn sản phẩm phù hợp
            - Sử dụng emoji để tạo cảm giác thân thiện
            """, contextInfo, userInput, formatUserNeeds(needs), formatProductsForAdvice(products));

        try {
            Prompt prompt = new Prompt(advicePrompt);
            return chatClient.call(prompt).getResult().getOutput().getContent().trim();
        } catch (Exception e) {
            return String.format("💡 Dựa trên yêu cầu của bạn, tôi gợi ý %d sản phẩm LEGO phù hợp được chọn lọc kỹ càng. " +
                    "Những sản phẩm này phù hợp với độ tuổi và sở thích của bạn.", products.size());
        }
    }

    private String generateBestSellingAdvice(String userInput, List<SanPham> products) {
        if (products.isEmpty()) {
            return "🔥 Hiện tại chưa có dữ liệu về sản phẩm bán chạy. Tôi sẽ tư vấn cho bạn một số sản phẩm LEGO phổ biến!";
        }

        try {
            String advicePrompt = String.format("""
                Bạn là chuyên gia tư vấn LEGO chuyên nghiệp.
                
                **YÊU CẦU KHÁCH HÀNG:** "%s"
                **SẢN PHẨM BÁN CHẠY:** %s
                
                Hãy viết lời tư vấn về sản phẩm bán chạy (150-200 từ):
                - Giải thích tại sao những sản phẩm này bán chạy
                - Đưa ra lời khuyên cụ thể cho từng sản phẩm
                - Nhấn mạnh ưu điểm và phù hợp với đối tượng nào
                - Sử dụng emoji để tạo cảm giác thân thiện
                - Kết thúc bằng lời khuyên chung
                """, userInput, formatProductsForAdvice(products));

            Prompt prompt = new Prompt(advicePrompt);
            return chatClient.call(prompt).getResult().getOutput().getContent().trim();
        } catch (Exception e) {
            System.err.println("Error in generateBestSellingAdvice: " + e.getMessage());
            return String.format("🔥 **TOP %d SẢN PHẨM BÁN CHẠY NHẤT** 🔥\n\n" +
                    "Dựa trên dữ liệu bán hàng 3 tháng gần nhất, đây là những sản phẩm LEGO được khách hàng yêu thích nhất:\n\n" +
                    "%s\n\n" +
                    "💡 **Lời khuyên:** Những sản phẩm này đều có chất lượng cao, phù hợp với nhiều độ tuổi và được đánh giá tốt từ khách hàng. " +
                    "Bạn có thể chọn theo sở thích hoặc liên hệ tôi để được tư vấn chi tiết hơn!",
                    products.size(), formatProductsForAdvice(products));
        }
    }

    private String formatUserNeeds(UserNeeds needs) {
        return String.format("Độ tuổi: %s, Sở thích: %s, Kinh nghiệm: %s, Danh mục: %s",
                needs.getAgeGroup() != null ? needs.getAgeGroup() : "N/A",
                needs.getInterests() != null ? needs.getInterests() : "N/A",
                needs.getExperience() != null ? needs.getExperience() : "N/A",
                needs.getCategory() != null ? needs.getCategory() : "N/A");
    }

    private FollowUpAnalysis analyzeFollowUpQuestion(String userInput, List<ChatMemory> chatHistory) {
        String contextInfo = buildContextFromHistory(chatHistory);
        
        String analysisPrompt = String.format("""
            Phân tích câu hỏi follow-up và xác định cần tìm kiếm sản phẩm không.
            
            **NGỮ CẢNH:** %s
            **CÂU HỎI:** "%s"
            
            Trả về JSON:
            {
              "analysis": "phân tích ngắn gọn về câu hỏi",
              "needProducts": true/false,
              "searchCriteria": "tiêu chí tìm kiếm nếu cần"
            }
            """, contextInfo, userInput);

        try {
            Prompt prompt = new Prompt(analysisPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            JsonNode node = objectMapper.readTree(cleanJson);
            
            String analysis = node.get("analysis").asText();
            boolean needProducts = node.get("needProducts").asBoolean();
            
            List<SanPham> products = new ArrayList<>();
            if (needProducts && node.has("searchCriteria")) {
                String criteria = node.get("searchCriteria").asText();
                products = searchProductsWithContext(criteria, chatHistory);
            }
            
            return new FollowUpAnalysis(analysis, products);
            
        } catch (Exception e) {
            return new FollowUpAnalysis("Câu hỏi follow-up về cuộc trò chuyện trước", new ArrayList<>());
        }
    }

    private List<SanPham> findAlternativeProducts(SearchRequestDTO originalCriteria) {
        try {
            // Tạo tiêu chí rộng hơn
            SearchRequestDTO relaxedCriteria = new SearchRequestDTO();

            if (originalCriteria.getDoTuoi() != null) {
                relaxedCriteria.setDoTuoi(originalCriteria.getDoTuoi());
            }
            if (originalCriteria.getGia() != null) {
                relaxedCriteria.setGia(originalCriteria.getGia());
            }

            List<SanPham> products = sanPhamRepo.timKiemTheoDieuKien(relaxedCriteria);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(1);
            // Nếu vẫn trống, lấy sản phẩm bán chạy
            if (products.isEmpty()) {
                List<Object[]> results = sanPhamRepo.findTopDaBan(startDate, endDate);
                List<String> tenSP = results.stream()
                        .map(r -> (String) r[1])
                        .toList();
                products = tenSP.stream()
                        .map(sanPhamRepo::findByTenSanPham)
                        .toList();
            }

            return products.stream().limit(8).collect(Collectors.toList());

        } catch (Exception e) {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(1);
            List<Object[]> results = sanPhamRepo.findTopDaBan(startDate, endDate);
            List<String> tenSP = results.stream()
                    .map(r -> (String) r[1])
                    .toList();
            return tenSP.stream()
                    .map(sanPhamRepo::findByTenSanPham)
                    .toList();
        }
    }

    private String determineFAQResponse(String lowerInput) {
        if (lowerInput.contains("bảo hành")) {
            return faqDatabase.get("WARRANTY");
        } else if (lowerInput.contains("thanh toán")) {
            return faqDatabase.get("PAYMENT");
        } else if (lowerInput.contains("đổi") || lowerInput.contains("trả")) {
            return faqDatabase.get("RETURN");
        } else if (lowerInput.contains("liên hệ")) {
            return faqDatabase.get("CONTACT");
        } else {
            return "📋 **Câu hỏi thường gặp:**\n\n" +
                    "🚚 " + faqDatabase.get("SHIPPING") + "\n\n" +
                    "🛡️ " + faqDatabase.get("WARRANTY") + "\n\n" +
                    "💳 " + faqDatabase.get("PAYMENT") + "\n\n" +
                    "🔄 " + faqDatabase.get("RETURN") + "\n\n" +
                    "📞 " + faqDatabase.get("CONTACT");
        }
    }

    private String formatProductsForAdvice(List<SanPham> products) {
        return products.stream()
                .limit(5)
                .map(p -> String.format("- %s | Giá: %s | Độ tuổi: %s",
                        p.getTenSanPham() != null ? p.getTenSanPham() : "N/A",
                        p.getGia() != null ? String.format("%,.0f đ", p.getGia()) : "N/A",
                        p.getDoTuoi() != null ? p.getDoTuoi() : "N/A"))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Utility Methods
     */
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
    public SanPhamResponseDTO convertToResponseDTO(SanPham sanPham) {
        List<AnhSp> listAnh = anhSpRepo.findBySanPhamId(sanPham.getId());
        List<AnhResponse> anhUrls = listAnh.stream()
                .map(anh -> {
                    AnhResponse response = new AnhResponse();
                    response.setId(anh.getId());
                    response.setUrl(anh.getUrl());
                    response.setAnhChinh(anh.getAnhChinh());
                    return response;
                })
                .toList();

        SanPhamResponseDTO dto = new SanPhamResponseDTO();
        dto.setId(sanPham.getId());
        dto.setTenSanPham(sanPham.getTenSanPham());
        dto.setMaSanPham(sanPham.getMaSanPham());
        dto.setDoTuoi(sanPham.getDoTuoi());
        dto.setMoTa(sanPham.getMoTa());
        dto.setGia(sanPham.getGia());
        dto.setSoLuongManhGhep(sanPham.getSoLuongManhGhep());
        dto.setSoLuongTon(sanPham.getSoLuongTon());
        dto.setSoLuongVote(sanPham.getSoLuongVote());
        dto.setDanhGiaTrungBinh(sanPham.getDanhGiaTrungBinh());
        dto.setDanhMucId(sanPham.getDanhMuc() != null ? sanPham.getDanhMuc().getId() : null);
        dto.setBoSuuTapId(sanPham.getBoSuuTap() != null ? sanPham.getBoSuuTap().getId() : null);
        dto.setXuatXuId(sanPham.getXuatXu() != null ? sanPham.getXuatXu().getId() : null);
        dto.setTrangThai(sanPham.getTrangThai());
        dto.setThuongHieuId(sanPham.getThuongHieu() != null ? sanPham.getThuongHieu().getId() : null);
        dto.setNoiBat(sanPham.getNoiBat() != null ? sanPham.getNoiBat() : null);
        dto.setAnhUrls(anhUrls);
        return dto;
    }
}

