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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import java.util.Map;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
    
    // Thread pool cho async operations
    private final Executor asyncExecutor = Executors.newFixedThreadPool(4);
    
    // Cache cho các kết quả tìm kiếm phổ biến
    private final Map<String, List<SanPham>> searchCache = new ConcurrentHashMap<>();
    private final Map<String, IntentClassificationDTO> intentCache = new ConcurrentHashMap<>();
    private final Map<String, List<SanPham>> bestSellingCache = new ConcurrentHashMap<>();
    
    // Cache TTL (Time To Live) - 5 phút
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5 phút

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
     * Main method xử lý input từ user với memory context - OPTIMIZED
     */
    public ChatResponse handleUserInput(String userInput, String sessionId) {
        try {
            // Lấy chat history để có context
            List<ChatMemory> chatHistory = getChatHistory(sessionId);

            // Phân loại intent với caching
            IntentClassificationDTO intent = classifyIntentWithCaching(userInput, chatHistory);

            // Xử lý theo intent
            ChatResponse response = processIntentRequest(intent, userInput, chatHistory);

            // Lưu vào memory async để không block response
            CompletableFuture.runAsync(() -> 
                saveChatMemory(sessionId, userInput, response.getMessage(), intent.getIntent()), 
                asyncExecutor);

            return response;

        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse("ERROR",
                    "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau hoặc liên hệ hotline để được hỗ trợ.", null);
            CompletableFuture.runAsync(() -> 
                saveChatMemory(sessionId, userInput, errorResponse.getMessage(), "ERROR"), 
                asyncExecutor);
            return errorResponse;
        }
    }

    /**
     * Phân loại intent với caching - OPTIMIZED
     */
    private IntentClassificationDTO classifyIntentWithCaching(String userInput, List<ChatMemory> chatHistory) {
        // Tạo cache key từ user input (normalize để cache hiệu quả hơn)
        String cacheKey = normalizeInputForCache(userInput);
        
        // Kiểm tra cache trước
        if (isCacheValid(cacheKey)) {
            IntentClassificationDTO cached = intentCache.get(cacheKey);
            if (cached != null) {
                System.out.println("DEBUG: Using cached intent: " + cached.getIntent());
                return cached;
            }
        }
        
        // Nếu không có cache, thực hiện phân loại
        IntentClassificationDTO result = classifyIntentWithContext(userInput, chatHistory);
        
        // Lưu vào cache
        intentCache.put(cacheKey, result);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        
        return result;
    }
    /**
     * Phân loại intent với context từ chat history - CẢI THIỆN
     */
    private IntentClassificationDTO classifyIntentWithContext(String userInput, List<ChatMemory> chatHistory) {
        // Sử dụng fallback logic trước khi gọi AI để tăng tốc
        String fallbackIntent = determineIntentFallback(userInput);
        
        // Ưu tiên fallback cho các câu tìm kiếm rõ ràng
        if (isClearSearchIntent(userInput)) {
            System.out.println("DEBUG: Using fallback for clear search intent: " + fallbackIntent);
            IntentClassificationDTO result = new IntentClassificationDTO();
            result.setIntent(fallbackIntent);
            result.setConfidence("HIGH");
            result.setExtractedInfo(userInput);
            return result;
        }
        
        // Chỉ gọi AI cho các trường hợp phức tạp và không rõ ràng
        if (isComplexIntent(userInput)) {
        String contextInfo = buildContextFromHistory(chatHistory);

        String intentPrompt = String.format("""
            Bạn là AI chuyên phân tích ý định khách hàng cho cửa hàng LEGO.
            
            **NGỮ CẢNH CUỘC TRÒ CHUYỆN:**
            %s
            
            **PHÂN TÍCH CÂU:** "%s"
            
            **QUY TẮC PHÂN LOẠI (CHỌN 1 INTENT DUY NHẤT - THEO THỨ TỰ ƯU TIÊN):**
                1. SHIPPING: Giao hàng, vận chuyển, thời gian ship, phí ship, "ship đến", "bao lâu", "miễn phí", "hoàn hàng", "đổi trả", "trả hàng", "hoàn tiền"
            2. ADVICE: Tư vấn, gợi ý, hỏi ý kiến về sản phẩm (ưu tiên cho "tư vấn", "bán chạy", "phổ biến")
            3. SEARCH: Tìm kiếm sản phẩm cụ thể (tên, loại, giá, thương hiệu, độ tuổi, xuất xứ)
            4. FAQ: Bảo hành, thanh toán, đổi trả, liên hệ
            5. FOLLOW_UP: Câu hỏi tiếp theo về cuộc trò chuyện trước
            6. GENERAL: Chào hỏi, cảm ơn, câu hỏi chung
            
                **LƯU Ý QUAN TRỌNG:** 
                - Ưu tiên SHIPPING nếu có từ: "ship", "giao", "bao lâu", "thời gian", "đến", "ha noi", "tp.hcm", "hoàn hàng", "đổi trả", "trả hàng", "hoàn tiền"
                - Nếu câu chứa "tìm", "cho", "tuổi", "xuất xứ" → SEARCH (nhưng không có từ shipping/return)
                - Chỉ trả về 1 intent duy nhất, không dùng dấu |
            
            **TRẢ VỀ JSON:**
            {
              "intent": "SEARCH|ADVICE|SHIPPING|FAQ|FOLLOW_UP|GENERAL",
              "confidence": "HIGH|MEDIUM|LOW",
              "extractedInfo": "thông tin quan trọng từ câu"
            }
            """, contextInfo, userInput);

        return executeIntentClassification(intentPrompt);
        } else {
            // Sử dụng fallback cho các trường hợp đơn giản
            IntentClassificationDTO result = new IntentClassificationDTO();
            result.setIntent(fallbackIntent);
            result.setConfidence("HIGH");
            result.setExtractedInfo(userInput);
            return result;
        }
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
     * Xử lý tìm kiếm sản phẩm - OPTIMIZED với caching
     */
    private ChatResponse handleProductSearch(String userInput, List<ChatMemory> chatHistory) {
        try {
            System.out.println("DEBUG: Starting product search for: " + userInput);
            
            // Kiểm tra cache trước (có thể disable cache để debug)
            String cacheKey = "search_" + normalizeInputForCache(userInput);
            List<SanPham> products = getCachedProducts(cacheKey);
            
            // Debug: Force fresh search if needed
            boolean forceFreshSearch = userInput.toLowerCase().contains("debug") || 
                                     userInput.toLowerCase().contains("fresh");
            
            if (products == null || forceFreshSearch) {
                if (forceFreshSearch) {
                    System.out.println("DEBUG: Force fresh search requested");
                }
                System.out.println("DEBUG: No cached products, searching...");
                
            // Tìm kiếm với context
                products = searchProductsWithContext(userInput, chatHistory);
                System.out.println("DEBUG: Context search found " + products.size() + " products");

            if (products.isEmpty()) {
                    System.out.println("DEBUG: Context search empty, trying fallback...");
                // Fallback: tìm kiếm rộng hơn
                products = searchProductsFallback(userInput);
                    System.out.println("DEBUG: Fallback search found " + products.size() + " products");
                
                    // Nếu vẫn không tìm thấy, thử tìm kiếm chỉ theo xuất xứ
                if (products.isEmpty()) {
                        System.out.println("DEBUG: Fallback empty, trying origin-only search...");
                        SearchRequestDTO originRequest = new SearchRequestDTO();
                        originRequest.setXuatXu("Trung Quốc");
                        products = searchWithRepository(originRequest);
                        System.out.println("DEBUG: Origin-only search found " + products.size() + " products");
                    }
                    
                    // Nếu vẫn không tìm thấy, thử tìm kiếm chỉ theo độ tuổi
                    if (products.isEmpty()) {
                        System.out.println("DEBUG: Origin search empty, trying age-only search...");
                        SearchRequestDTO ageRequest = new SearchRequestDTO();
                        ageRequest.setDoTuoi(7);
                        products = searchWithRepository(ageRequest);
                        System.out.println("DEBUG: Age-only search found " + products.size() + " products");
                    }
                    
                    if (products.isEmpty()) {
                        System.out.println("DEBUG: No products found, returning empty result");
                    return new ChatResponse("SEARCH",
                            "Không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. " +
                            "Bạn có thể:\n• Thử tìm kiếm với từ khóa khác\n• Cho tôi biết thêm thông tin (độ tuổi, ngân sách, sở thích)\n• Liên hệ hotline để được tư vấn trực tiếp", null);
                }
            }

                // Filter products before caching - chỉ filter theo trạng thái
                List<SanPham> activeProducts = products.stream()
                        .filter(p -> p.getTrangThai() != null && 
                                   !p.getTrangThai().equals("Ngừng kinh doanh") &&
                                   !p.getTrangThai().equals("Hết hàng"))
                    .collect(Collectors.toList());
                
                System.out.println("DEBUG: Filtered products: " + products.size() + 
                                 " -> " + activeProducts.size() + " active products");
                
                // Debug: Count products with null origin
                long nullOriginCount = products.stream()
                        .filter(p -> p.getXuatXu() == null)
                        .count();
                if (nullOriginCount > 0) {
                    System.out.println("DEBUG: Found " + nullOriginCount + " products with null origin (filtered out)");
                }
                
                // Cache kết quả (chỉ cache sản phẩm active)
                cacheProducts(cacheKey, activeProducts);
                products = activeProducts;
            } else {
                System.out.println("DEBUG: Using cached products: " + products.size());
            }

            // Convert to DTO - OPTIMIZED batch conversion
            List<SanPhamResponseDTO> productDTOs = convertToResponseDTOs(products);
            System.out.println("DEBUG: Converted to " + productDTOs.size() + " DTOs");

            String message = generateSearchResponse(userInput, products, chatHistory);
            return new ChatResponse("SEARCH", message, productDTOs);

        } catch (Exception e) {
            System.err.println("DEBUG: Error in handleProductSearch: " + e.getMessage());
            e.printStackTrace();
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
                
                try {
                    List<SanPham> bestSellingProducts = findBestSellingProducts();
                    System.out.println("Found " + bestSellingProducts.size() + " best selling products");
                    
                    if (bestSellingProducts.isEmpty()) {
                        // Fallback: lấy tất cả sản phẩm
                        bestSellingProducts = sanPhamRepo.findAll().stream()
                                .limit(3)
                                .collect(Collectors.toList());
                        System.out.println("Fallback: Found " + bestSellingProducts.size() + " products");
                    }
                    
                    // Convert to DTO - OPTIMIZED batch conversion
                    List<SanPhamResponseDTO> bestSellingDTOs = convertToResponseDTOs(bestSellingProducts);
                    
                    String adviceMessage = generateBestSellingAdvice(userInput, bestSellingProducts);
                    return new ChatResponse("ADVICE", adviceMessage, bestSellingDTOs);
                    
                } catch (Exception e) {
                    System.err.println("Error finding best selling products: " + e.getMessage());
                    // Fallback: lấy tất cả sản phẩm
                    List<SanPham> allProducts = sanPhamRepo.findAll().stream()
                            .limit(3)
                            .collect(Collectors.toList());
                    
                    // Convert to DTO - OPTIMIZED batch conversion
                    List<SanPhamResponseDTO> allProductDTOs = convertToResponseDTOs(allProducts);
                    
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
            
            // Convert to DTO - OPTIMIZED batch conversion
            List<SanPhamResponseDTO> recommendedDTOs = convertToResponseDTOs(recommendedProducts);
            
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

            // Convert products to DTO - OPTIMIZED batch conversion
            List<SanPhamResponseDTO> productDTOs = convertToResponseDTOs(analysis.getProducts());

            return new ChatResponse("FOLLOW_UP", response.trim(), productDTOs);

        } catch (Exception e) {
            return new ChatResponse("FOLLOW_UP",
                    "Tôi hiểu bạn đang hỏi thêm về cuộc trò chuyện trước. " +
                    "Bạn có thể nói rõ hơn để tôi hỗ trợ tốt hơn không?", null);
        }
    }

    /**
     * Xử lý shipping query - CẢI THIỆN (bao gồm hoàn hàng)
     */
    private ChatResponse handleShippingQuery(String userInput) {
        String lowerInput = userInput.toLowerCase();
        String response = "";

        // Xử lý câu hỏi về hoàn hàng/đổi trả
        if (lowerInput.contains("hoàn hàng") || lowerInput.contains("đổi trả") || lowerInput.contains("trả hàng") || lowerInput.contains("hoàn tiền")) {
            response = "🔄 **Chính sách hoàn hàng & đổi trả:**\n\n";
            response += "📋 **Điều kiện hoàn hàng:**\n";
            response += "• Đổi trả không lý do trong 7 ngày đầu\n";
            response += "• Sản phẩm phải nguyên vẹn, chưa sử dụng\n";
            response += "• Có hóa đơn mua hàng hợp lệ\n";
            response += "• Đầy đủ phụ kiện, bao bì gốc\n\n";
            
            response += "⏰ **Thời gian xử lý:**\n";
            response += "• Xác nhận yêu cầu: 1-2 ngày làm việc\n";
            response += "• Hoàn tiền: 3-7 ngày làm việc\n";
            response += "• Đổi sản phẩm mới: 5-10 ngày làm việc\n\n";
            
            response += "💰 **Phí hoàn hàng:**\n";
            response += "• Miễn phí: Lỗi từ cửa hàng\n";
            response += "• Phí ship: 30,000đ (khách hàng đổi ý)\n";
            response += "• Hoàn tiền: Miễn phí\n\n";
            
            response += "📞 **Liên hệ hoàn hàng:**\n";
            response += "• Hotline: 1900-xxxx\n";
            response += "• Email: return@legoshop.vn\n";
            response += "• Zalo: zalo.me/legoshop";
            
            return new ChatResponse("SHIPPING", response, null);
        }

        // Xử lý câu hỏi về giao hàng thông thường
        String shippingInfo = faqDatabase.get("SHIPPING");

        // Xử lý câu hỏi cụ thể về thời gian giao hàng
        if (lowerInput.contains("thời gian") || lowerInput.contains("bao lâu")) {
            if (lowerInput.contains("ha noi") || lowerInput.contains("hà nội")) {
                shippingInfo += "\n\n🏙️ **Giao hàng tại Hà Nội:**\n• Nội thành: 1-2 ngày làm việc\n• Ngoại thành: 3-5 ngày làm việc\n• Giao trong ngày: Đặt trước 14:00 (+30,000đ)";
            } else if (lowerInput.contains("tp.hcm") || lowerInput.contains("sài gòn") || lowerInput.contains("hồ chí minh")) {
                shippingInfo += "\n\n🏙️ **Giao hàng tại TP.HCM:**\n• Nội thành: 1-2 ngày làm việc\n• Ngoại thành: 3-5 ngày làm việc\n• Giao trong ngày: Đặt trước 14:00 (+30,000đ)";
            } else {
                shippingInfo += "\n\n⏰ **Thời gian giao hàng chi tiết:**\n• Giao trong ngày: Đặt trước 14:00 (+30,000đ)\n• Giao nhanh: +50,000đ (giao trong 2-4 giờ)\n• Giao tiêu chuẩn: Miễn phí (1-5 ngày tùy khu vực)";
            }
        }
        
        if (lowerInput.contains("miễn phí") || lowerInput.contains("free")) {
            shippingInfo += "\n\n💡 **Miễn phí ship:**\n• Đơn hàng từ 500,000đ: Miễn phí hoàn toàn\n• Đơn dưới 500,000đ: Phí ship 30,000đ";
        }
        
        if (lowerInput.contains("phí") || lowerInput.contains("cost")) {
            shippingInfo += "\n\n💰 **Phí giao hàng:**\n• Miễn phí: Đơn từ 500,000đ\n• Phí chuẩn: 30,000đ (đơn dưới 500,000đ)\n• Giao nhanh: +50,000đ\n• Giao trong ngày: +30,000đ";
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
        
        // SHIPPING - ưu tiên cao nhất cho shipping (trước SEARCH để tránh conflict)
        if (lowerInput.contains("ship") || lowerInput.contains("giao") || lowerInput.contains("vận chuyển") ||
            lowerInput.contains("thời gian") || lowerInput.contains("bao lâu") || lowerInput.contains("miễn phí") ||
            lowerInput.contains("phí ship") || lowerInput.contains("giao hàng") || lowerInput.contains("đến") ||
            lowerInput.contains("ha noi") || lowerInput.contains("hà nội") || lowerInput.contains("tp.hcm") ||
            lowerInput.contains("sài gòn") || lowerInput.contains("hồ chí minh") || lowerInput.contains("hoàn hàng") ||
            lowerInput.contains("đổi trả") || lowerInput.contains("trả hàng") || lowerInput.contains("hoàn tiền")) {
            System.out.println("DEBUG: Detected SHIPPING intent");
            return "SHIPPING";
        }
        
        // ADVICE - ưu tiên cao cho tư vấn
        if (lowerInput.contains("tư vấn") || lowerInput.contains("gợi ý") || lowerInput.contains("khuyên") ||
            lowerInput.contains("bán chạy") || lowerInput.contains("phổ biến") || lowerInput.contains("nổi tiếng") ||
            lowerInput.contains("đề xuất") || lowerInput.contains("giới thiệu")) {
            System.out.println("DEBUG: Detected ADVICE intent");
            return "ADVICE";
        }
        
        // SEARCH - tìm kiếm sản phẩm cụ thể (loại bỏ các từ có thể conflict với SHIPPING)
        if (lowerInput.contains("tìm") || lowerInput.contains("mua") || lowerInput.contains("có") || 
            lowerInput.contains("lego") || lowerInput.contains("xe") || lowerInput.contains("robot") ||
            lowerInput.contains("sản phẩm") || lowerInput.contains("đồ chơi") || lowerInput.contains("cho tôi") ||
            lowerInput.contains("tuổi") || lowerInput.contains("xuất xứ") || 
            lowerInput.contains("trung quốc") || lowerInput.contains("đức") || lowerInput.contains("mỹ") || 
            lowerInput.contains("nhật") || lowerInput.contains("hàn quốc")) {
            System.out.println("DEBUG: Detected SEARCH intent");
            return "SEARCH";
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
        try {
            // Cải thiện: Sử dụng logic phân tích đơn giản trước khi gọi AI
            SearchRequestDTO request = parseSearchRequestFromInput(userInput);
            
            if (request.getTen() != null || request.getDoTuoi() != null || request.getXuatXu() != null) {
                System.out.println("DEBUG: Using parsed search criteria: " + request);
                List<SanPham> products = searchWithNativeQuery(request);
                System.out.println("DEBUG: Found " + products.size() + " products with parsed criteria");
                return products;
            }
            
            // Nếu không parse được, mới dùng AI
        String contextInfo = buildContextFromHistory(chatHistory);
        
        String searchPrompt = String.format("""
            Bạn là chuyên gia phân tích tìm kiếm LEGO.
            
            **NGỮ CẢNH:** %s
            **CÂU TÌM KIẾM:** "%s"
            
            Trả về JSON với thông tin tìm kiếm:
            {
              "ten": null,
              "gia": null,
              "doTuoi": "độ tuổi (ví dụ: 5, 6, 12, 18)",
              "xuatXu": "xuất xứ (ví dụ: Trung Quốc, Đức, Mỹ, Thái Lan)",
              "thuongHieu": "thương hiệu LEGO",
              "boSuuTap": "bộ sưu tập",
              "soLuongManhGhepMin": null,
              "danhGiaToiThieu": null
            }
            
            **QUAN TRỌNG - PHÂN BIỆT RÕ RÀNG:**
            - "ten": Chỉ điền tên sản phẩm cụ thể (ví dụ: "LEGO Technic", "LEGO City", "LEGO Star Wars")
            - "xuatXu": Điền xuất xứ sản xuất (ví dụ: "Trung Quốc", "Đức", "Mỹ", "Thái Lan", "Nhật Bản")
            - KHÔNG điền "ten" cho: "trẻ 12 tuổi", "cho trẻ em", "sản phẩm", "Thái Lan", "Trung Quốc"
            - KHÔNG điền "xuatXu" cho tên sản phẩm
            - Nếu câu có "xuất xứ Thái Lan" → xuatXu = "Thái Lan", ten = null
            - Nếu câu có "LEGO Technic" → ten = "LEGO Technic", xuatXu = null (trừ khi có xuất xứ)
            - Chỉ điền thông tin chắc chắn, để null nếu không rõ
            """, contextInfo, userInput);

        try {
            Prompt prompt = new Prompt(searchPrompt);
            String jsonResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            String cleanJson = cleanJsonResponse(jsonResponse);
            String processedJson = preprocessJsonNumbers(cleanJson);
                SearchRequestDTO aiRequest = objectMapper.readValue(processedJson, SearchRequestDTO.class);
                System.out.println("DEBUG: AI parsed search criteria: " + aiRequest);
                List<SanPham> products = searchWithRepository(aiRequest);
                System.out.println("DEBUG: Found " + products.size() + " products with AI criteria");
                return products;
        } catch (Exception e) {
                System.err.println("DEBUG: AI parsing failed, using fallback: " + e.getMessage());
                return searchProductsFallback(userInput);
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Search failed, using fallback: " + e.getMessage());
            e.printStackTrace();
            return searchProductsFallback(userInput);
        }
    }

    private List<SanPham> searchProductsFallback(String userInput) {
        // Fallback: tìm kiếm theo từ khóa đơn giản - OPTIMIZED
        try {
            String[] keywords = userInput.toLowerCase().split("\\s+");
            if (keywords.length == 0 || keywords[0].length() <= 2) {
                return new ArrayList<>();
            }
            
            // Sử dụng repository method thay vì load tất cả sản phẩm
            String searchTerm = keywords[0];
            List<SanPham> products = sanPhamRepo.findByTenSanPhamContainingIgnoreCase(searchTerm);
            
            // Nếu không tìm thấy, thử tìm kiếm rộng hơn
            if (products.isEmpty()) {
                // Thử tìm kiếm với từ khóa khác
                for (String keyword : keywords) {
                    if (keyword.length() > 2) {
                        products = sanPhamRepo.findByTenSanPhamContainingIgnoreCase(keyword);
                        if (!products.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            
            return products.stream()
                    .limit(3)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in searchProductsFallback: " + e.getMessage());
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
            
            List<SanPham> products = searchWithRepository(criteria);
            
            // If no products found, try alternative search
            if (products.isEmpty()) {
                products = findAlternativeProducts(criteria);
            }
            
            return products.stream().limit(3).collect(Collectors.toList());
            
        } catch (Exception e) {
            return findAlternativeProducts(new SearchRequestDTO());
        }
    }

    /**
     * Tìm sản phẩm bán chạy nhất - OPTIMIZED với caching
     */
    private List<SanPham> findBestSellingProducts() {
        String cacheKey = "best_selling";
        
        // Kiểm tra cache trước
        if (isCacheValid(cacheKey)) {
            List<SanPham> cached = bestSellingCache.get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                System.out.println("DEBUG: Using cached best selling products: " + cached.size());
                return cached;
            }
        }
        
        try {
            // Thử lấy dữ liệu bán chạy từ 6 tháng gần nhất (rộng hơn)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(6);
            
            System.out.println("Searching for best selling products from " + startDate + " to " + endDate);
            
            List<Object[]> results = sanPhamRepo.findTopDaBan(startDate, endDate);
            System.out.println("Found " + results.size() + " results from database");
            
            if (results.isEmpty()) {
                System.out.println("No best selling data found, using fallback");
                List<SanPham> fallback = getFallbackProducts();
                cacheBestSelling(cacheKey, fallback);
                return fallback;
            }
            
            List<String> tenSP = results.stream()
                    .map(r -> (String) r[1])
                    .limit(3) // Lấy top 3 sản phẩm bán chạy
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
                List<SanPham> fallback = getFallbackProducts();
                cacheBestSelling(cacheKey, fallback);
                return fallback;
            }
            
            // Cache kết quả
            cacheBestSelling(cacheKey, products);
            return products;
                    
        } catch (Exception e) {
            System.err.println("Error in findBestSellingProducts: " + e.getMessage());
            e.printStackTrace();
            List<SanPham> fallback = getFallbackProducts();
            cacheBestSelling(cacheKey, fallback);
            return fallback;
        }
    }
    
    /**
     * Fallback: lấy sản phẩm phổ biến - OPTIMIZED
     */
    private List<SanPham> getFallbackProducts() {
        try {
            // Sử dụng repository method để lấy sản phẩm có đánh giá tốt
            List<SanPham> topRatedProducts = sanPhamRepo.findTop3ByDanhGiaTrungBinhGreaterThanOrderByDanhGiaTrungBinhDesc(0.0);
            
            if (!topRatedProducts.isEmpty()) {
                System.out.println("Fallback: Found " + topRatedProducts.size() + " top rated products");
                return topRatedProducts;
            }
            
            // Nếu không có sản phẩm có đánh giá, lấy sản phẩm mới nhất
            List<SanPham> recentProducts = sanPhamRepo.findTop3ByOrderByIdDesc();
            System.out.println("Fallback: Found " + recentProducts.size() + " recent products");
            return recentProducts;
            
        } catch (Exception e) {
            System.err.println("Error in getFallbackProducts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Integer extractAgeFromGroup(String ageGroup) {
        if (ageGroup.contains("trẻ em") || ageGroup.contains("nhỏ")) {
            return 6;
        } else if (ageGroup.contains("thiếu niên")) {
            return 12;
        } else if (ageGroup.contains("người lớn")) {
            return 18;
        }
        return 18;
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

            List<SanPham> products = searchWithRepository(relaxedCriteria);

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
     * Search Request Parsing - IMPROVED
     */
    private SearchRequestDTO parseSearchRequestFromInput(String userInput) {
        SearchRequestDTO request = new SearchRequestDTO();
        String lowerInput = userInput.toLowerCase();
        
        // Parse độ tuổi (sử dụng logic phù hợp với database)
        if (lowerInput.contains("5 tuổi") || lowerInput.contains("5 tuoi")) {
            request.setDoTuoi(5);
        } else if (lowerInput.contains("6 tuổi") || lowerInput.contains("6 tuoi")) {
            request.setDoTuoi(6);
        } else if (lowerInput.contains("7 tuổi") || lowerInput.contains("7 tuoi")) {
            request.setDoTuoi(7);
        } else if (lowerInput.contains("8 tuổi") || lowerInput.contains("8 tuoi")) {
            request.setDoTuoi(8);
        } else if (lowerInput.contains("9 tuổi") || lowerInput.contains("9 tuoi")) {
            request.setDoTuoi(9);
        } else if (lowerInput.contains("10 tuổi") || lowerInput.contains("10 tuoi")) {
            request.setDoTuoi(10);
        } else if (lowerInput.contains("11 tuổi") || lowerInput.contains("11 tuoi")) {
            request.setDoTuoi(11);
        } else if (lowerInput.contains("12 tuổi") || lowerInput.contains("12 tuoi")) {
            request.setDoTuoi(12);
        } else if (lowerInput.contains("18 tuổi") || lowerInput.contains("18 tuoi")) {
            request.setDoTuoi(18);
        } else if (lowerInput.contains("trẻ em") || lowerInput.contains("tre em")) {
            request.setDoTuoi(6);
        } else if (lowerInput.contains("thiếu niên") || lowerInput.contains("thieu nien")) {
            request.setDoTuoi(12);
        } else if (lowerInput.contains("người lớn") || lowerInput.contains("nguoi lon")) {
            request.setDoTuoi(18);
        }
        
        // Parse xuất xứ - Handle encoding issues (ƯU TIÊN CAO)
        if (lowerInput.contains("trung quốc") || lowerInput.contains("trung quoc")) {
            request.setXuatXu("Trung Quốc"); // Database has encoding issue
        } else if (lowerInput.contains("đức") || lowerInput.contains("duc")) {
            request.setXuatXu("Đức");
        } else if (lowerInput.contains("mĩ") || lowerInput.contains("mi")) {
            request.setXuatXu("Mĩ"); // Database has encoding issue
        } else if (lowerInput.contains("nhật") || lowerInput.contains("nhat") || lowerInput.contains("nhật bản")) {
            request.setXuatXu("Nhật Bản"); // Database has encoding issue
        } else if (lowerInput.contains("hàn quốc") || lowerInput.contains("han quoc")) {
            request.setXuatXu("Hàn Quốc");
        } else if (lowerInput.contains("đan mạch") || lowerInput.contains("dan mach")) {
            request.setXuatXu("Đan mạch");
        } else if (lowerInput.contains("việt nam") || lowerInput.contains("viet nam")) {
            request.setXuatXu("Việt nam");
        } else if (lowerInput.contains("thái lan") || lowerInput.contains("thai lan")) {
            request.setXuatXu("Thái Lan");
        }
        
        // Parse thương hiệu
        if (lowerInput.contains("lego")) {
            request.setThuongHieu("LEGO");
        }
        
        // Parse tên sản phẩm (chỉ khi KHÔNG có xuất xứ được parse)
        if (request.getXuatXu() == null) {
            String[] words = userInput.split("\\s+");
            List<String> productWords = new ArrayList<>();
            
            for (String word : words) {
                String lowerWord = word.toLowerCase();
                if (!lowerWord.contains("tuổi") && !lowerWord.contains("tuoi") &&
                    !lowerWord.contains("trung") && !lowerWord.contains("quốc") && !lowerWord.contains("quoc") &&
                    !lowerWord.contains("đức") && !lowerWord.contains("duc") &&
                    !lowerWord.contains("mỹ") && !lowerWord.contains("my") &&
                    !lowerWord.contains("nhật") && !lowerWord.contains("nhat") &&
                    !lowerWord.contains("hàn") && !lowerWord.contains("han") &&
                    !lowerWord.contains("quốc") && !lowerWord.contains("quoc") &&
                    !lowerWord.contains("đan") && !lowerWord.contains("mạch") && !lowerWord.contains("mach") &&
                    !lowerWord.contains("việt") && !lowerWord.contains("viet") && !lowerWord.contains("nam") &&
                    !lowerWord.contains("thái") && !lowerWord.contains("thai") && !lowerWord.contains("lan") &&
                    !lowerWord.contains("lego") && !lowerWord.contains("cho") && !lowerWord.contains("tôi") &&
                    !lowerWord.contains("tìm") && !lowerWord.contains("tim") &&
                    !lowerWord.contains("sản") && !lowerWord.contains("san") &&
                    !lowerWord.contains("phẩm") && !lowerWord.contains("pham") &&
                    !lowerWord.contains("và") && !lowerWord.contains("va") &&
                    !lowerWord.contains("xuất") && !lowerWord.contains("xuat") &&
                    !lowerWord.contains("xứ") && !lowerWord.contains("xu") &&
                    !lowerWord.contains("trẻ") && !lowerWord.contains("tre") &&
                    !lowerWord.contains("giúp") && !lowerWord.contains("giup") &&
                    word.length() > 1) {
                    productWords.add(word);
                }
            }
            
            // Chỉ set tên nếu có từ khóa sản phẩm thực sự (không phải từ mô tả)
            if (!productWords.isEmpty()) {
                String productName = String.join(" ", productWords);
                // Kiểm tra xem có phải là tên sản phẩm thực sự không
                if (!productName.toLowerCase().contains("tuổi") && 
                    !productName.toLowerCase().contains("trẻ") &&
                    !productName.toLowerCase().contains("cho") &&
                    !productName.toLowerCase().contains("xuất") &&
                    !productName.toLowerCase().contains("xứ") &&
                    productName.length() > 2) {
                    request.setTen(productName);
                }
            }
        }
        
        System.out.println("DEBUG: Parsed search request: " + request);
        return request;
    }

    /**
     * Cache Management Methods - OPTIMIZED
     */
    private String normalizeInputForCache(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "_")
                .trim();
    }
    
    private boolean isCacheValid(String cacheKey) {
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_TTL;
    }
    
    private List<SanPham> getCachedProducts(String cacheKey) {
        if (isCacheValid(cacheKey)) {
            List<SanPham> cachedProducts = searchCache.get(cacheKey);
            if (cachedProducts != null) {
                // Filter out products that are not in business - chỉ filter theo trạng thái
                List<SanPham> activeProducts = cachedProducts.stream()
                        .filter(p -> p.getTrangThai() != null && 
                                   !p.getTrangThai().equals("Ngừng kinh doanh") &&
                                   !p.getTrangThai().equals("Hết hàng"))
                        .collect(Collectors.toList());
                
                System.out.println("DEBUG: Cached products: " + cachedProducts.size() + 
                                 ", Active products: " + activeProducts.size());
                
                return activeProducts.isEmpty() ? null : activeProducts;
            }
        }
        return null;
    }
    
    private void cacheProducts(String cacheKey, List<SanPham> products) {
        searchCache.put(cacheKey, products);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis());
    }
    
    private void cacheBestSelling(String cacheKey, List<SanPham> products) {
        bestSellingCache.put(cacheKey, products);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis());
    }
    
    private boolean isClearSearchIntent(String userInput) {
        String lowerInput = userInput.toLowerCase();
        // Kiểm tra các từ khóa tìm kiếm rõ ràng
        return (lowerInput.contains("tìm") && lowerInput.contains("cho")) ||
               (lowerInput.contains("tìm") && lowerInput.contains("tuổi")) ||
               (lowerInput.contains("tìm") && lowerInput.contains("xuất xứ")) ||
               (lowerInput.contains("cho") && lowerInput.contains("tuổi")) ||
               (lowerInput.contains("cho") && lowerInput.contains("xuất xứ")) ||
               (lowerInput.contains("tuổi") && lowerInput.contains("xuất xứ")) ||
               (lowerInput.contains("trung quốc") || lowerInput.contains("đức") || 
                lowerInput.contains("mỹ") || lowerInput.contains("nhật"));
    }
    
    private boolean isComplexIntent(String userInput) {
        String lowerInput = userInput.toLowerCase();
        // Chỉ gọi AI cho các trường hợp phức tạp
        return lowerInput.length() > 20 || 
               lowerInput.contains("và") || 
               lowerInput.contains("hoặc") ||
               lowerInput.contains("nhưng") ||
               lowerInput.contains("tuy nhiên") ||
               lowerInput.split("\\s+").length > 8;
    }
    
    /**
     * Clear cache periodically (có thể gọi từ scheduler)
     */
    public void clearExpiredCache() {
        long currentTime = System.currentTimeMillis();
        cacheTimestamps.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > CACHE_TTL);
        
        // Clear corresponding cache entries
        searchCache.keySet().removeIf(key -> !cacheTimestamps.containsKey(key));
        intentCache.keySet().removeIf(key -> !cacheTimestamps.containsKey(key));
        bestSellingCache.keySet().removeIf(key -> !cacheTimestamps.containsKey(key));
    }
    
    /**
     * Clear all cache manually
     */
    public void clearAllCache() {
        searchCache.clear();
        intentCache.clear();
        bestSellingCache.clear();
        cacheTimestamps.clear();
        System.out.println("DEBUG: All cache cleared");
    }
    
    /**
     * Clear search cache only
     */
    public void clearSearchCache() {
        searchCache.clear();
        cacheTimestamps.entrySet().removeIf(entry -> 
            entry.getKey().startsWith("search_"));
        System.out.println("DEBUG: Search cache cleared");
    }

    
    /**
     * Method để gọi native query với DTO object (wrapper)
     */
    private List<SanPham> searchWithNativeQuery(SearchRequestDTO request) {
        return searchWithRepository(request);
    }
    
    /**
     * Method để gọi repository với DTO object
     */
    private List<SanPham> searchWithRepository(SearchRequestDTO request) {
        try {
            System.out.println("DEBUG: searchWithRepository called with:");
            System.out.println("  - doTuoi: " + request.getDoTuoi());
            System.out.println("  - ten: " + request.getTen());
            System.out.println("  - gia: " + request.getGia());
            System.out.println("  - xuatXu: " + request.getXuatXu());
            
            return sanPhamRepo.timKiemTheoDieuKien(request);
        } catch (Exception e) {
            System.err.println("DEBUG: Repository search failed: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Method để gọi native query với encoding handling
     */
    private List<SanPham> searchWithNativeQuery(Integer doTuoi, String ten, BigDecimal gia, 
                                               String xuatXu, String thuongHieu, String boSuuTap,
                                               Integer soLuongManhGhepMin, Double danhGiaToiThieu) {
        try {
            SearchRequestDTO request = new SearchRequestDTO();
            request.setDoTuoi(doTuoi);
            request.setTen(ten);
            request.setGia(gia);
            request.setXuatXu(xuatXu);
            request.setThuongHieu(thuongHieu);
            request.setBoSuuTap(boSuuTap);
            request.setSoLuongManhGhepMin(soLuongManhGhepMin);
            request.setDanhGiaToiThieu(danhGiaToiThieu);
            
            List<SanPham> products = sanPhamRepo.timKiemTheoDieuKien(request);
            
            System.out.println("DEBUG: Native query found " + products.size() + " products");
            return products;
            
        } catch (Exception e) {
            System.err.println("DEBUG: Native query failed: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Test method để kiểm tra native query
     */
    public void testNativeQuery() {
        try {
            System.out.println("DEBUG: Testing native query...");
            
            // Test với các input khác nhau
            String[] testInputs = {
                "Tìm giúp tôi sản phẩm xuất xứ từ nhat",
                "Tìm giúp tôi sản phẩm xuất xứ từ Nhật Bản",
                "Tìm giúp tôi sản phẩm xuất xứ từ mỹ",
                "Tìm giúp tôi sản phẩm xuất xứ từ trung quốc"
            };
            
            for (String input : testInputs) {
                System.out.println("---");
                System.out.println("DEBUG: Testing input: " + input);
                
                // Test parsing
                SearchRequestDTO request = parseSearchRequestFromInput(input);
                System.out.println("DEBUG: Parsed request: " + request);
                
                // Test native query
                List<SanPham> products = searchWithRepository(request);
                System.out.println("DEBUG: Native query results: " + products.size() + " products");
                
                if (!products.isEmpty()) {
                    System.out.println("DEBUG: Sample product: " + products.get(0).getTenSanPham() + 
                        " | Origin: " + (products.get(0).getXuatXu() != null ? products.get(0).getXuatXu().getTen() : "NULL"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("DEBUG: Native query test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Debug method để trace search flow cụ thể
     */
    public String debugSearchFlow() {
        try {
            System.out.println("=== DEBUG SEARCH FLOW ===");
            
            String userInput = "Tìm giúp tôi sản phẩm cho trẻ 12 tuổi";
            System.out.println("1. User input: " + userInput);
            
            // Test parseSearchRequestFromInput
            SearchRequestDTO request = parseSearchRequestFromInput(userInput);
            System.out.println("2. Parsed request: " + request);
            System.out.println("   - doTuoi: " + request.getDoTuoi());
            System.out.println("   - ten: " + request.getTen());
            System.out.println("   - gia: " + request.getGia());
            System.out.println("   - xuatXu: " + request.getXuatXu());
            
            // Test searchWithRepository
            System.out.println("3. Calling searchWithRepository...");
            List<SanPham> products = searchWithRepository(request);
            System.out.println("4. Search results: " + products.size() + " products");
            
            if (!products.isEmpty()) {
                System.out.println("   Sample products:");
                products.stream().limit(3).forEach(p -> 
                    System.out.println("   - " + p.getTenSanPham() + " | Age: " + p.getDoTuoi() + " | Status: " + p.getTrangThai()));
            }
            
            // Test direct repository call
            System.out.println("5. Testing direct repository call...");
            SearchRequestDTO directRequest = new SearchRequestDTO();
            directRequest.setDoTuoi(12);
            List<SanPham> directProducts = sanPhamRepo.timKiemTheoDieuKien(directRequest);
            System.out.println("6. Direct repository results: " + directProducts.size() + " products");
            
            return "Debug completed - check console for full trace";
            
        } catch (Exception e) {
            System.err.println("Debug search flow failed: " + e.getMessage());
            e.printStackTrace();
            return "Debug failed: " + e.getMessage();
        }
    }
    
    /**
     * Debug method đơn giản để test query
     */
    public String testSimpleQuery() {
        try {
            System.out.println("=== TESTING SIMPLE QUERY ===");
            
            // Test 1: Query tất cả sản phẩm
            List<SanPham> allProducts = sanPhamRepo.findAll();
            System.out.println("Total products: " + allProducts.size());
            
            if (allProducts.isEmpty()) {
                return "❌ NO PRODUCTS IN DATABASE";
            }
            
            // Test 2: Query với điều kiện đơn giản
            SearchRequestDTO testRequest = new SearchRequestDTO();
            testRequest.setDoTuoi(12);
            List<SanPham> testResults = sanPhamRepo.timKiemTheoDieuKien(testRequest);
            System.out.println("Query results for age <= 12: " + testResults.size());
            
            // Test 3: Query không có điều kiện
            SearchRequestDTO allRequest = new SearchRequestDTO();
            List<SanPham> allResults = sanPhamRepo.timKiemTheoDieuKien(allRequest);
            System.out.println("Query results with no conditions: " + allResults.size());
            
            // Test 4: Query với xuất xứ Mĩ
            SearchRequestDTO originRequest = new SearchRequestDTO();
            originRequest.setXuatXu("Mĩ");
            List<SanPham> originResults = sanPhamRepo.timKiemTheoDieuKien(originRequest);
            System.out.println("Query results for origin Mĩ: " + originResults.size());
            
            // Test 5: Query với xuất xứ Mĩ và độ tuổi 18
            SearchRequestDTO combinedRequest = new SearchRequestDTO();
            combinedRequest.setXuatXu("Mĩ");
            combinedRequest.setDoTuoi(18);
            List<SanPham> combinedResults = sanPhamRepo.timKiemTheoDieuKien(combinedRequest);
            System.out.println("Query results for origin Mĩ and age 18: " + combinedResults.size());
            
            // Test 6: Test parsing "12 tuổi"
            System.out.println("\n=== TESTING PARSING ===");
            String userInput = "Tìm giúp tôi sản phẩm cho trẻ 12 tuổi";
            SearchRequestDTO parsedRequest = parseSearchRequestFromInput(userInput);
            System.out.println("Parsed request: " + parsedRequest);
            System.out.println("Parsed doTuoi: " + parsedRequest.getDoTuoi());
            
            // Test 7: Test search với parsed request
            List<SanPham> parsedResults = sanPhamRepo.timKiemTheoDieuKien(parsedRequest);
            System.out.println("Parsed search results: " + parsedResults.size());
            
            return "Test completed - check console for details. Results: " + testResults.size() + " products found";
            
        } catch (Exception e) {
            System.err.println("Simple query test failed: " + e.getMessage());
            e.printStackTrace();
            return "Test failed: " + e.getMessage();
        }
    }
    
    /**
     * Debug method để kiểm tra dữ liệu database
     */
    public String debugDatabaseData() {
        try {
            System.out.println("=== DEBUG DATABASE DATA ===");
            
            // 1. Kiểm tra tất cả sản phẩm
            List<SanPham> allProducts = sanPhamRepo.findAll();
            System.out.println("Total products in DB: " + allProducts.size());
            
            // 2. Kiểm tra trạng thái sản phẩm
            Map<String, Long> statusCount = allProducts.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getTrangThai() != null ? p.getTrangThai() : "NULL",
                    Collectors.counting()
                ));
            System.out.println("Product status distribution:");
            statusCount.forEach((status, count) -> 
                System.out.println("  " + status + ": " + count));
            
            // 3. Kiểm tra độ tuổi
            Map<Integer, Long> ageCount = allProducts.stream()
                .filter(p -> p.getDoTuoi() != null)
                .collect(Collectors.groupingBy(SanPham::getDoTuoi, Collectors.counting()));
            System.out.println("Age distribution:");
            ageCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> 
                    System.out.println("  Age " + entry.getKey() + ": " + entry.getValue()));
            
            // 4. Kiểm tra sản phẩm phù hợp với điều kiện
            List<SanPham> activeProducts = allProducts.stream()
                .filter(p -> p.getTrangThai() != null && p.getTrangThai().contains("Đang kinh doanh"))
                .collect(Collectors.toList());
            System.out.println("Active products: " + activeProducts.size());
            
            List<SanPham> age12Products = activeProducts.stream()
                .filter(p -> p.getDoTuoi() != null && p.getDoTuoi() <= 12)
                .collect(Collectors.toList());
            System.out.println("Active products age <= 12: " + age12Products.size());
            
            // 5. Hiển thị một vài sản phẩm mẫu
            System.out.println("Sample active products:");
            activeProducts.stream().limit(5).forEach(p -> 
                System.out.println("  - " + p.getTenSanPham() + 
                    " | Age: " + p.getDoTuoi() + 
                    " | Status: " + p.getTrangThai()));
            
            // 6. Test query trực tiếp
            System.out.println("\n=== TESTING DIRECT QUERY ===");
            try {
                SearchRequestDTO queryRequest = new SearchRequestDTO();
                queryRequest.setDoTuoi(12);
                List<SanPham> queryResults = sanPhamRepo.timKiemTheoDieuKien(queryRequest);
                System.out.println("Direct query results: " + queryResults.size());
                
                if (!queryResults.isEmpty()) {
                    System.out.println("Query result samples:");
                    queryResults.stream().limit(3).forEach(p -> 
                        System.out.println("  - " + p.getTenSanPham() + 
                            " | Age: " + p.getDoTuoi() + 
                            " | Status: " + p.getTrangThai()));
                }
                
                // Test query với xuất xứ Mĩ
                System.out.println("\n=== TESTING ORIGIN QUERY ===");
                SearchRequestDTO originRequest = new SearchRequestDTO();
                originRequest.setXuatXu("Mĩ");
                List<SanPham> originResults = sanPhamRepo.timKiemTheoDieuKien(originRequest);
                System.out.println("Origin Mĩ query results: " + originResults.size());
                
                // Test query với xuất xứ Mĩ và độ tuổi 18
                SearchRequestDTO combinedRequest = new SearchRequestDTO();
                combinedRequest.setXuatXu("Mĩ");
                combinedRequest.setDoTuoi(18);
                List<SanPham> combinedResults = sanPhamRepo.timKiemTheoDieuKien(combinedRequest);
                System.out.println("Origin Mĩ + Age 18 query results: " + combinedResults.size());
                
            } catch (Exception e) {
                System.err.println("Direct query failed: " + e.getMessage());
                e.printStackTrace();
            }
            
            return "Debug completed - check console for details";
            
        } catch (Exception e) {
            System.err.println("Database debug failed: " + e.getMessage());
            e.printStackTrace();
            return "Debug failed: " + e.getMessage();
        }
    }
    
    /**
     * Test method để kiểm tra parsing
     */
    public String testParsing() {
        try {
            System.out.println("=== TESTING PARSING ===");
            
            String userInput = "Tìm giúp tôi sản phẩm cho trẻ 12 tuổi";
            System.out.println("User input: " + userInput);
            
            SearchRequestDTO parsedRequest = parseSearchRequestFromInput(userInput);
            System.out.println("Parsed request: " + parsedRequest);
            System.out.println("Parsed doTuoi: " + parsedRequest.getDoTuoi());
            System.out.println("Parsed ten: " + parsedRequest.getTen());
            System.out.println("Parsed xuatXu: " + parsedRequest.getXuatXu());
            
            // Test search với parsed request
            List<SanPham> parsedResults = sanPhamRepo.timKiemTheoDieuKien(parsedRequest);
            System.out.println("Parsed search results: " + parsedResults.size());
            
            if (!parsedResults.isEmpty()) {
                System.out.println("Sample results:");
                parsedResults.stream().limit(3).forEach(p -> 
                    System.out.println("  - " + p.getTenSanPham() + 
                        " | Age: " + p.getDoTuoi() + 
                        " | Status: " + p.getTrangThai()));
            }
            
            // Test với query đơn giản hơn
            System.out.println("\n=== TESTING SIMPLE QUERIES ===");
            
            // Test 1: Query không có điều kiện
            SearchRequestDTO emptyRequest = new SearchRequestDTO();
            List<SanPham> emptyResults = sanPhamRepo.timKiemTheoDieuKien(emptyRequest);
            System.out.println("Empty query results: " + emptyResults.size());
            
            // Test 2: Query chỉ với doTuoi
            SearchRequestDTO ageRequest = new SearchRequestDTO();
            ageRequest.setDoTuoi(12);
            List<SanPham> ageResults = sanPhamRepo.timKiemTheoDieuKien(ageRequest);
            System.out.println("Age 12 query results: " + ageResults.size());
            
            // Test 3: Query với doTuoi = null
            SearchRequestDTO nullAgeRequest = new SearchRequestDTO();
            nullAgeRequest.setDoTuoi(null);
            List<SanPham> nullAgeResults = sanPhamRepo.timKiemTheoDieuKien(nullAgeRequest);
            System.out.println("Null age query results: " + nullAgeResults.size());
            
            // Return detailed info instead of just message
            return "Parsing test completed. Parsed doTuoi: " + parsedRequest.getDoTuoi() + 
                   ", Search results: " + parsedResults.size() + " products. " +
                   "Empty query: " + emptyResults.size() + ", Age 12: " + ageResults.size() + 
                   ", Null age: " + nullAgeResults.size();
            
        } catch (Exception e) {
            System.err.println("Parsing test failed: " + e.getMessage());
            e.printStackTrace();
            return "Parsing test failed: " + e.getMessage();
        }
    }
    
    /**
     * Test method đơn giản để kiểm tra API
     */
    public String testSimpleAPI() {
        try {
            System.out.println("=== TESTING SIMPLE API ===");
            
            // Test 1: Query tất cả sản phẩm
            List<SanPham> allProducts = sanPhamRepo.findAll();
            System.out.println("Total products: " + allProducts.size());
            
            // Test 2: Query với điều kiện đơn giản
            SearchRequestDTO request = new SearchRequestDTO();
            request.setDoTuoi(12);
            List<SanPham> results = sanPhamRepo.timKiemTheoDieuKien(request);
            System.out.println("Query results: " + results.size());
            
            return "API test completed. Total: " + allProducts.size() + ", Query results: " + results.size();
            
        } catch (Exception e) {
            System.err.println("API test failed: " + e.getMessage());
            e.printStackTrace();
            return "API test failed: " + e.getMessage();
        }
    }
    
    /**
     * Test method để kiểm tra search flow đơn giản
     */
    public void testSimpleSearch() {
        try {
            System.out.println("DEBUG: Testing simple search...");
            
            // Test 1: Search for age 12
            String userInput = "Tìm giúp tôi sản phẩm cho trẻ 12 tuổi";
            System.out.println("DEBUG: User input: " + userInput);
            
            SearchRequestDTO request = parseSearchRequestFromInput(userInput);
            System.out.println("DEBUG: Parsed request: " + request);
            
            List<SanPham> products = searchWithRepository(request);
            System.out.println("DEBUG: Search results: " + products.size() + " products");
            
            if (!products.isEmpty()) {
                System.out.println("DEBUG: Sample products:");
                products.forEach(p -> System.out.println("  - " + p.getTenSanPham() + 
                    " | Age: " + p.getDoTuoi() + 
                    " | Status: " + p.getTrangThai()));
            }
            
            // Test 2: Direct repository call
            System.out.println("---");
            System.out.println("DEBUG: Testing direct repository call...");
            SearchRequestDTO directRequest = new SearchRequestDTO();
            directRequest.setDoTuoi(12);
            List<SanPham> directProducts = sanPhamRepo.timKiemTheoDieuKien(directRequest);
            System.out.println("DEBUG: Direct call results: " + directProducts.size() + " products");
            
            // Test 3: Check all products
            System.out.println("---");
            System.out.println("DEBUG: Checking all products...");
            List<SanPham> allProducts = sanPhamRepo.findAll();
            System.out.println("DEBUG: Total products: " + allProducts.size());
            
            long activeProducts = allProducts.stream()
                    .filter(p -> p.getTrangThai() != null && p.getTrangThai().contains("Đang kinh doanh"))
                    .count();
            System.out.println("DEBUG: Active products: " + activeProducts);
            
            long age12Products = allProducts.stream()
                    .filter(p -> p.getDoTuoi() != null && p.getDoTuoi() <= 12)
                    .count();
            System.out.println("DEBUG: Products age <= 12: " + age12Products);
            
        } catch (Exception e) {
            System.err.println("DEBUG: Simple search test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test method tổng hợp để kiểm tra tất cả các trường hợp parsing
     */
    public String testComprehensiveParsing() {
        try {
            System.out.println("=== COMPREHENSIVE PARSING TEST ===");
            
            String[] testCases = {
                "Tìm giúp tôi sản phẩm cho trẻ 12 tuổi",
                "Tìm giúp tôi sản phẩm xuất xứ Thái Lan", 
                "Tìm giúp tôi sản phẩm xuất xứ Trung Quốc",
                "Tìm giúp tôi sản phẩm LEGO Technic",
                "Tìm giúp tôi sản phẩm cho trẻ 5 tuổi xuất xứ Đức",
                "Tìm giúp tôi sản phẩm LEGO City xuất xứ Mỹ"
            };
            
            StringBuilder results = new StringBuilder();
            results.append("=== COMPREHENSIVE PARSING TEST RESULTS ===\n\n");
            
            for (String testInput : testCases) {
                System.out.println("---");
                System.out.println("Testing: " + testInput);
                
                SearchRequestDTO request = parseSearchRequestFromInput(testInput);
                System.out.println("Result: " + request);
                
                results.append("Input: ").append(testInput).append("\n");
                results.append("  - doTuoi: ").append(request.getDoTuoi()).append("\n");
                results.append("  - ten: ").append(request.getTen() != null ? request.getTen() : "null").append("\n");
                results.append("  - xuatXu: ").append(request.getXuatXu() != null ? request.getXuatXu() : "null").append("\n");
                results.append("  - thuongHieu: ").append(request.getThuongHieu() != null ? request.getThuongHieu() : "null").append("\n");
                results.append("\n");
            }
            
            return results.toString();
            
        } catch (Exception e) {
            System.err.println("Comprehensive test failed: " + e.getMessage());
            e.printStackTrace();
            return "❌ COMPREHENSIVE TEST FAILED: " + e.getMessage();
        }
    }

    /**
     * Test method để kiểm tra fix cho xuất xứ Thái Lan
     */
    public String testFixThaiLan() {
        try {
            System.out.println("=== TESTING FIX FOR 'xuất xứ Thái Lan' ===");
            
            String userInput = "Tìm giúp tôi sản phẩm xuất xứ Thái Lan";
            System.out.println("User input: " + userInput);
            
            // Test parsing
            SearchRequestDTO request = parseSearchRequestFromInput(userInput);
            System.out.println("Parsed request: " + request);
            System.out.println("Parsed doTuoi: " + request.getDoTuoi());
            System.out.println("Parsed ten: " + request.getTen());
            System.out.println("Parsed xuatXu: " + request.getXuatXu());
            
            // Test search
            List<SanPham> products = searchWithRepository(request);
            System.out.println("Search results: " + products.size() + " products");
            
            if (!products.isEmpty()) {
                System.out.println("Sample products:");
                products.stream().limit(3).forEach(p -> 
                    System.out.println("  - " + p.getTenSanPham() + 
                        " | Origin: " + (p.getXuatXu() != null ? p.getXuatXu().getTen() : "NULL") + 
                        " | Status: " + p.getTrangThai()));
                
                return "✅ FIX SUCCESSFUL! Found " + products.size() + " products from Thailand. " +
                       "Parsed xuatXu: " + request.getXuatXu() + ", ten: " + 
                       (request.getTen() != null ? request.getTen() : "null (correct)");
            } else {
                return "❌ FIX FAILED! No products found. Check database or query logic.";
            }
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            return "❌ TEST FAILED: " + e.getMessage();
        }
    }

    /**
     * Test method để kiểm tra fix cho "trẻ 12 tuổi"
     */
    public String testFix12Tuoi() {
        try {
            System.out.println("=== TESTING FIX FOR 'trẻ 12 tuổi' ===");
            
            String userInput = "Tìm giúp tôi sản phẩm cho trẻ 12 tuổi";
            System.out.println("User input: " + userInput);
            
            // Test parsing
            SearchRequestDTO request = parseSearchRequestFromInput(userInput);
            System.out.println("Parsed request: " + request);
            System.out.println("Parsed doTuoi: " + request.getDoTuoi());
            System.out.println("Parsed ten: " + request.getTen());
            System.out.println("Parsed xuatXu: " + request.getXuatXu());
            
            // Test search
            List<SanPham> products = searchWithRepository(request);
            System.out.println("Search results: " + products.size() + " products");
            
            if (!products.isEmpty()) {
                System.out.println("Sample products:");
                products.stream().limit(3).forEach(p -> 
                    System.out.println("  - " + p.getTenSanPham() + 
                        " | Age: " + p.getDoTuoi() + 
                        " | Status: " + p.getTrangThai()));
                
                return "✅ FIX SUCCESSFUL! Found " + products.size() + " products for age 12. " +
                       "Parsed doTuoi: " + request.getDoTuoi() + ", ten: " + 
                       (request.getTen() != null ? request.getTen() : "null (correct)");
            } else {
                return "❌ FIX FAILED! No products found. Check database or query logic.";
            }
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            return "❌ TEST FAILED: " + e.getMessage();
        }
    }

    /**
     * Test method để kiểm tra search flow cho trẻ 12 tuổi
     */
    public void testSearchFlow12Tuoi() {
        try {
            System.out.println("DEBUG: Testing search flow for 12 tuổi...");
            
            String userInput = "Tìm giúp tôi sản phẩm cho trẻ 12 tuổi";
            System.out.println("DEBUG: User input: " + userInput);
            
            // Test parsing
            SearchRequestDTO request = parseSearchRequestFromInput(userInput);
            System.out.println("DEBUG: Parsed request: " + request);
            
            // Test search
            List<SanPham> products = searchWithRepository(request);
            System.out.println("DEBUG: Search results: " + products.size() + " products");
            
            if (!products.isEmpty()) {
                System.out.println("DEBUG: Sample products:");
                products.forEach(p -> System.out.println("  - " + p.getTenSanPham() + 
                    " | Age: " + p.getDoTuoi() + 
                    " | Status: " + p.getTrangThai()));
            } else {
                System.out.println("DEBUG: No products found, checking database...");
                
                // Test direct query
                List<SanPham> allProducts = sanPhamRepo.findAll();
                System.out.println("DEBUG: Total products in database: " + allProducts.size());
                
                // Check products with age 12
                List<SanPham> age12Products = allProducts.stream()
                        .filter(p -> p.getDoTuoi() != null && p.getDoTuoi() <= 12)
                        .collect(Collectors.toList());
                System.out.println("DEBUG: Products with age <= 12: " + age12Products.size());
                
                if (!age12Products.isEmpty()) {
                    System.out.println("DEBUG: Sample age 12 products:");
                    age12Products.forEach(p -> System.out.println("  - " + p.getTenSanPham() + 
                        " | Age: " + p.getDoTuoi() + 
                        " | Status: " + p.getTrangThai()));
                }
            }
            
        } catch (Exception e) {
            System.err.println("DEBUG: Search flow test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test method để kiểm tra encoding fix
     */
    public void testEncodingFix() {
        try {
            System.out.println("DEBUG: Testing encoding fix...");
            
            // Test với các input khác nhau
            String[] testInputs = {
                "Tìm giúp tôi sản phẩm xuất xứ từ nhat",
                "Tìm giúp tôi sản phẩm xuất xứ từ Nhật Bản",
                "Tìm giúp tôi sản phẩm xuất xứ từ mỹ",
                "Tìm giúp tôi sản phẩm xuất xứ từ trung quốc"
            };
            
            for (String input : testInputs) {
                System.out.println("---");
                System.out.println("DEBUG: Testing input: " + input);
                
                // Test parsing
                SearchRequestDTO request = parseSearchRequestFromInput(input);
                System.out.println("DEBUG: Parsed origin: '" + request.getXuatXu() + "'");
                
                // Test search with native query
                List<SanPham> products = searchWithRepository(request);
                System.out.println("DEBUG: Search results: " + products.size() + " products");
                
                if (!products.isEmpty()) {
                    System.out.println("DEBUG: Sample product: " + products.get(0).getTenSanPham() + 
                        " | Origin: " + (products.get(0).getXuatXu() != null ? products.get(0).getXuatXu().getTen() : "NULL"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("DEBUG: Encoding fix test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

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
        // OPTIMIZED: Batch load images để tránh N+1 queries
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
    
    /**
     * Batch convert products to DTOs - OPTIMIZED để tránh N+1 queries
     */
    public List<SanPhamResponseDTO> convertToResponseDTOs(List<SanPham> sanPhams) {
        if (sanPhams.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Batch load tất cả images cho tất cả sản phẩm
        List<Integer> sanPhamIds = sanPhams.stream()
                .map(SanPham::getId)
                .collect(Collectors.toList());
        
        List<AnhSp> allImages = anhSpRepo.findBySanPhamIdIn(sanPhamIds);
        Map<Integer, List<AnhSp>> imagesBySanPhamId = allImages.stream()
                .collect(Collectors.groupingBy(anhSp -> anhSp.getSanPham().getId()));
        
        // Convert to DTOs
        return sanPhams.stream()
                .map(sanPham -> {
                    List<AnhSp> productImages = imagesBySanPhamId.getOrDefault(sanPham.getId(), new ArrayList<>());
                    List<AnhResponse> anhUrls = productImages.stream()
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
                })
                .collect(Collectors.toList());
    }
}

