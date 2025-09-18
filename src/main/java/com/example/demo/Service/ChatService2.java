package com.example.demo.Service;

import com.example.demo.Entity.AnhSp;
import com.example.demo.Entity.SanPham;
import com.example.demo.Enum.ChatIntent;
import com.example.demo.Repository.Anh_sp_Repo;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.AnhResponse;
import com.example.demo.Responses.ChatResponse;
import com.example.demo.Responses.SanPhamResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService2 {
    private final San_pham_Repo productRepository;
    private final Anh_sp_Repo anhSpRepo;

    public ChatIntent detectIntent(String prompt) {
        String lower = prompt.toLowerCase();
        if (lower.contains("giảm giá") || lower.contains("tìm") || lower.contains("lego") || lower.contains("sản phẩm"))
            return ChatIntent.TIM_KIEM;
        if (lower.contains("tư vấn") || lower.contains("phù hợp"))
            return ChatIntent.TU_VAN;
        if (lower.contains("ship") || lower.contains("giao") || lower.contains("bao lâu"))
            return ChatIntent.SHIP;
        if (lower.contains("chính sách") || lower.contains("bảo hành") || lower.contains("đổi trả"))
            return ChatIntent.GIOI_THIEU_CHINH_SACH;
        if (lower.contains("chào") || lower.contains("xin chào") || lower.contains("hi")|| lower.contains("hello"))
            return ChatIntent.CHAO_HOI;
        return ChatIntent.KHAC;
    }
    public ChatResponse handlePrompt(String prompt) {
        ChatIntent intent = detectIntent(prompt);

        switch (intent) {
            case TIM_KIEM:
                return handleSearch(prompt);

            case TU_VAN:
                return new ChatResponse(
                        "ADVICE",
                        "Bạn muốn tư vấn về độ tuổi hay xuất xứ nào?",
                        null
                );

            case GIOI_THIEU_CHINH_SACH:
                return new ChatResponse(
                        "FAQ",
                        "🔄 **Chính sách đổi trả:**\n• Đổi trả trong 7 ngày\n• Sản phẩm nguyên vẹn, chưa sử dụng\n• Miễn phí nếu lỗi từ cửa hàng",
                        null
                );

            case CHAO_HOI:
                return new ChatResponse(
                        "GENERAL",
                        "👋 Xin chào! Tôi có thể giúp bạn:\n🔍 Tìm sản phẩm LEGO\n💡 Tư vấn mua hàng\n🚚 Thông tin giao hàng\n🛡️ Chính sách bảo hành",
                        null
                );

            case SHIP:
                return new ChatResponse(
                        "SHIPPING",
                        "🚚 **Giao hàng:**\n• Nội thành: 1–2 ngày\n• Ngoại thành: 3–5 ngày\n• Miễn phí cho đơn từ 500k",
                        null
                );

            default:
                return new ChatResponse(
                        "GENERAL",
                        "Xin lỗi, tôi chưa hiểu yêu cầu. Bạn có thể diễn đạt rõ hơn không?",
                        null
                );
        }
    }

    private ChatResponse handleSearch(String prompt) {
        String lower = prompt.toLowerCase();
        List<SanPham> results;

        if (lower.contains("mới nhất") || lower.contains("mới thêm") || lower.contains("sản phẩm mới") || lower.contains("cập nhật mới")) {
            results = productRepository.findTop3ByTrangThaiOrderByNgayTaoDesc("Đang kinh doanh");
            return buildSearchResponse("Sản phẩm mới nhất vừa cập bến cho bạn", results);
        }

        if (lower.contains("bán chạy") || lower.contains("hot")|| lower.contains("bestseller") || lower.contains("phổ biến")) {
            results = productRepository.findBestSeller();
            return buildSearchResponse("Sản phẩm đang hot và bán chạy nhất hiện nay", results);
        }

        if (lower.contains("độ tuổi") || lower.contains("tuổi")) {
            int age = 0;
            Matcher matcher = Pattern.compile("(\\d{1,2})").matcher(lower);
            if (matcher.find()) {
                age = Integer.parseInt(matcher.group(1));
            }
            results = productRepository.findByTrangThaiAndDoTuoiBetween("Đang kinh doanh",age, 99);
            return buildSearchResponse(
                    "Sản phẩm phù hợp cho độ tuổi " + age + "+", results
            );
        }

        String keyword = extractKeyword(lower);
        results = productRepository
                .searchActive(
                        "Đang kinh doanh",keyword);
        return buildSearchResponse("Kết quả tìm kiếm cho từ khóa: " + keyword, results);
    }

    private ChatResponse buildSearchResponse(String title, List<SanPham> list) {
        if (list.isEmpty()) {
            return new ChatResponse("SEARCH",
                    "Rất tiếc, hiện tại chúng tôi không tìm thấy " + title.toLowerCase() + ". " +
                            "Bạn có muốn thử tìm với từ khóa khác hoặc cần gợi ý về các sản phẩm phù hợp không? 😊",
                    Collections.emptyList());
        }
        List<SanPhamResponseDTO> dtos =
                convertToResponseDTOs(list);

        return new ChatResponse("SEARCH", "🔎 " + title, dtos);
    }

    private String extractKeyword(String lower) {
        return lower.replace("tìm", "")
                .replace("lego", "")
                .replace("sản phẩm", "")
                .replace("xuất xứ", "")
                .trim();
    }
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
