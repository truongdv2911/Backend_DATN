package com.example.demo.Service;

import com.example.demo.DTOs.SanPhamDTO;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.Responses.SanPhamResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class San_pham_Service {
    private final San_pham_Repo sanPhamRepository;
    private final Anh_sp_Repo anhSpRepo;
    private final Danh_muc_Repo danhMucRepository;
    private final Bo_suu_tap_Repo boSuuTapRepository;
    private final Khuyen_mai_Repo khuyenMaiRepository;

    // Phương thức helper tính trạng thái theo số lượng tồn
    private String tinhTrangThaiTheoTonKho(int soLuongTon) {
        return soLuongTon > 0 ? "Còn hàng" : "Hết hàng";
    }

    public SanPhamResponseDTO createSanPham(@Valid SanPhamDTO sanPhamDTO) {
        String maSanPham = sanPhamDTO.getMaSanPham();
        if (maSanPham == null || maSanPham.isBlank()) {
            int maxTry = 10;
            int count = 0;
            do {
                maSanPham = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã san pham mới, vui lòng thử lại!");
                }
            } while (sanPhamRepository.existsByMaSanPham(maSanPham));
        } else {
            if (sanPhamRepository.existsByMaSanPham(maSanPham)) {
                throw new RuntimeException("Mã san pham đã tồn tại!");
            }
        }

        SanPham sanPham = new SanPham();
        sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
        sanPham.setMaSanPham(maSanPham);
        sanPham.setDoTuoi(sanPhamDTO.getDoTuoi());
        sanPham.setMoTa(sanPhamDTO.getMoTa());
        sanPham.setGia(sanPhamDTO.getGia());

        BigDecimal giaGoc = sanPhamDTO.getGia();
        BigDecimal giaKhuyenMai = giaGoc; // mặc định bằng giá gốc

        if (sanPhamDTO.getKhuyenMaiId() != null) {
            KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
                    .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
            if (!"Đang hoạt động".equalsIgnoreCase(khuyenMai.getTrangThai())) {
                throw new RuntimeException("Khuyến mãi không còn hoạt động và không thể áp dụng.");
            }
            sanPham.setKhuyenMai(khuyenMai);
            if (khuyenMai.getPhanTramGiam() != null && khuyenMai.getPhanTramGiam() > 0) {
                BigDecimal phanTram = new BigDecimal(khuyenMai.getPhanTramGiam()).divide(BigDecimal.valueOf(100));
                giaKhuyenMai = giaGoc.subtract(giaGoc.multiply(phanTram));
            }
        }

        sanPham.setGiaKhuyenMai(giaKhuyenMai);
        sanPham.setSoLuong(sanPhamDTO.getSoLuong());
        sanPham.setSoLuongManhGhep(sanPhamDTO.getSoLuongManhGhep());
        sanPham.setSoLuongTon(sanPhamDTO.getSoLuongTon());
        sanPham.setAnhDaiDien(sanPhamDTO.getAnhDaiDien());
        sanPham.setSoLuongVote(0);
        sanPham.setDanhGiaTrungBinh(0.0);

        if (sanPhamDTO.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(sanPhamDTO.getDanhMucId())
                    .orElseThrow(() -> new RuntimeException("DanhMuc not found with id: " + sanPhamDTO.getDanhMucId()));
            sanPham.setDanhMuc(danhMuc);
        }

        if (sanPhamDTO.getBoSuuTapId() != null) {
            BoSuuTap boSuuTap = boSuuTapRepository.findById(sanPhamDTO.getBoSuuTapId())
                    .orElseThrow(() -> new RuntimeException("BoSuuTap not found with id: " + sanPhamDTO.getBoSuuTapId()));
            sanPham.setBoSuuTap(boSuuTap);
        }

        // Gán trạng thái theo số lượng tồn
        sanPham.setTrangThai(tinhTrangThaiTheoTonKho(sanPham.getSoLuongTon()));

        
        SanPham savedSanPham = sanPhamRepository.save(sanPham);
        return convertToResponseDTO(savedSanPham);
    }

    public List<SanPhamResponseDTO> getAllSanPhamResponses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SanPham> pageSanPhams = sanPhamRepository.findAll(pageable);

        List<SanPhamResponseDTO> dtoList = new ArrayList<>();

        for (SanPham sp : pageSanPhams.getContent()) {
            List<AnhSp> listAnh = anhSpRepo.findBySanPhamId(sp.getId());

            List<String> anhUrls = listAnh.stream()
                    .map(AnhSp::getUrl)
                    .collect(Collectors.toList());

            SanPhamResponseDTO dto = new SanPhamResponseDTO();
            dto.setId(sp.getId());
            dto.setTenSanPham(sp.getTenSanPham());
            dto.setMaSanPham(sp.getMaSanPham());
            dto.setDoTuoi(sp.getDoTuoi());
            dto.setMoTa(sp.getMoTa());
            dto.setGia(sp.getGia());
            dto.setGiaKhuyenMai(sp.getGiaKhuyenMai());
            dto.setSoLuong(sp.getSoLuong());
            dto.setSoLuongManhGhep(sp.getSoLuongManhGhep());
            dto.setSoLuongTon(sp.getSoLuongTon());
            dto.setAnhDaiDien(sp.getAnhDaiDien());
            dto.setSoLuongVote(sp.getSoLuongVote());
            dto.setDanhGiaTrungBinh(sp.getDanhGiaTrungBinh());
            dto.setTenDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null);
            dto.setTenBoSuuTap(sp.getBoSuuTap() != null ? sp.getBoSuuTap().getTenBoSuuTap() : null);
            dto.setKhuyenMaiId(sp.getKhuyenMai() != null ? sp.getKhuyenMai().getId() : null);
            dto.setTrangThai(sp.getTrangThai());
            dto.setAnhUrls(anhUrls);

            dtoList.add(dto);
        }

        return dtoList;
    }

    public SanPham getSanPhamById(Integer id) {
        try {
            return sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching SanPham by id: " + e.getMessage(), e);
        }
    }

    public SanPhamResponseDTO updateSanPham(Integer id, @Valid SanPhamDTO sanPhamDTO) {
        String maSanPham = sanPhamDTO.getMaSanPham();
        if (maSanPham == null || maSanPham.isBlank()) {
            int maxTry = 10;
            int count = 0;
            do {
                maSanPham = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã san pham mới, vui lòng thử lại!");
                }
            } while (sanPhamRepository.existsByMaSanPham(maSanPham));
        }
        try {
            SanPham sanPham = getSanPhamById(id);
            sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
            sanPham.setMaSanPham(maSanPham);
            sanPham.setDoTuoi(sanPhamDTO.getDoTuoi());
            sanPham.setMoTa(sanPhamDTO.getMoTa());
            sanPham.setGia(sanPhamDTO.getGia());

            BigDecimal giaGoc = sanPhamDTO.getGia();
            BigDecimal giaKhuyenMai = giaGoc; // mặc định bằng giá gốc

            if (sanPhamDTO.getKhuyenMaiId() != null) {
                KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
                        .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
                if (!"Đang hoạt động".equalsIgnoreCase(khuyenMai.getTrangThai())) {
                    throw new RuntimeException("Khuyến mãi không còn hoạt động và không thể áp dụng.");
                }
                sanPham.setKhuyenMai(khuyenMai);

                if (khuyenMai.getPhanTramGiam() != null && khuyenMai.getPhanTramGiam() > 0) {
                    BigDecimal phanTram = new BigDecimal(khuyenMai.getPhanTramGiam()).divide(BigDecimal.valueOf(100));
                    giaKhuyenMai = giaGoc.subtract(giaGoc.multiply(phanTram));
                }
            }

            sanPham.setGiaKhuyenMai(giaKhuyenMai);

            sanPham.setSoLuong(sanPhamDTO.getSoLuong());
            sanPham.setSoLuongManhGhep(sanPhamDTO.getSoLuongManhGhep());
            sanPham.setSoLuongTon(sanPhamDTO.getSoLuongTon());
            sanPham.setAnhDaiDien(null);
            sanPham.setSoLuongVote(0);
            sanPham.setDanhGiaTrungBinh(0.0);

            if (sanPhamDTO.getDanhMucId() != null) {
                DanhMuc danhMuc = danhMucRepository.findById(sanPhamDTO.getDanhMucId())
                        .orElseThrow(() -> new RuntimeException("DanhMuc not found with id: " + sanPhamDTO.getDanhMucId()));
                sanPham.setDanhMuc(danhMuc);
            }

            if (sanPhamDTO.getBoSuuTapId() != null) {
                BoSuuTap boSuuTap = boSuuTapRepository.findById(sanPhamDTO.getBoSuuTapId())
                        .orElseThrow(() -> new RuntimeException("BoSuuTap not found with id: " + sanPhamDTO.getBoSuuTapId()));
                sanPham.setBoSuuTap(boSuuTap);
            }

            // Gán trạng thái theo số lượng tồn
            sanPham.setTrangThai(tinhTrangThaiTheoTonKho(sanPham.getSoLuongTon()));

            SanPham savedSanPham = sanPhamRepository.save(sanPham);
            return convertToResponseDTO(savedSanPham);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating SanPham: " + e.getMessage(), e);
        }
    }

    public void deleteSanPham(Integer id) {
        try {
            SanPham sanPham = getSanPhamById(id);
            sanPhamRepository.delete(sanPham);
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting SanPham: " + e.getMessage(), e);
        }
    }
    public SanPhamResponseDTO convertToResponseDTO(SanPham sanPham) {
        List<AnhSp> listAnh = anhSpRepo.findBySanPhamId(sanPham.getId());
        List<String> anhUrls = listAnh.stream()
                .map(AnhSp::getUrl)
                .collect(Collectors.toList());

        SanPhamResponseDTO dto = new SanPhamResponseDTO();
        dto.setId(sanPham.getId());
        dto.setTenSanPham(sanPham.getTenSanPham());
        dto.setMaSanPham(sanPham.getMaSanPham());
        dto.setDoTuoi(sanPham.getDoTuoi());
        dto.setMoTa(sanPham.getMoTa());
        dto.setGia(sanPham.getGia());
        dto.setGiaKhuyenMai(sanPham.getGiaKhuyenMai());
        dto.setSoLuong(sanPham.getSoLuong());
        dto.setSoLuongManhGhep(sanPham.getSoLuongManhGhep());
        dto.setSoLuongTon(sanPham.getSoLuongTon());
        dto.setAnhDaiDien(sanPham.getAnhDaiDien());
        dto.setSoLuongVote(sanPham.getSoLuongVote());
        dto.setDanhGiaTrungBinh(sanPham.getDanhGiaTrungBinh());
        dto.setTenDanhMuc(sanPham.getDanhMuc() != null ? sanPham.getDanhMuc().getTenDanhMuc() : null);
        dto.setTenBoSuuTap(sanPham.getBoSuuTap() != null ? sanPham.getBoSuuTap().getTenBoSuuTap() : null);
        dto.setKhuyenMaiId(sanPham.getKhuyenMai() != null ? sanPham.getKhuyenMai().getId() : null);
        dto.setTrangThai(sanPham.getTrangThai());
        dto.setAnhUrls(anhUrls);

        return dto;
    }

    private String generateMaPhieu() {
        Random random = new Random();
        int randomNumber = random.nextInt(900000) + 100000;
        return "SP" + randomNumber;
    }
}
