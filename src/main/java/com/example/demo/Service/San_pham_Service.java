package com.example.demo.Service;

import com.example.demo.DTOs.SanPhamDTO;
import com.example.demo.DTOs.dtoRespone.SanPhamResponseDTO;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class San_pham_Service {

    private final San_pham_Repo sanPhamRepository;
    private final Danh_muc_Repo danhMucRepository;
    private final Bo_suu_tap_Repo boSuuTapRepository;
    private final Khuyen_mai_Repo khuyenMaiRepository;

    public SanPham createSanPham(@Valid SanPhamDTO sanPhamDTO) {
        // Kiểm tra trùng mã sản phẩm
        if (sanPhamRepository.existsByMaSanPham(sanPhamDTO.getMaSanPham())) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại: " + sanPhamDTO.getMaSanPham());
        }

        // Tạo mới sản phẩm
        SanPham sanPham = new SanPham();
        sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
        sanPham.setMaSanPham(sanPhamDTO.getMaSanPham());
        sanPham.setDoTuoi(sanPhamDTO.getDoTuoi());
        sanPham.setMoTa(sanPhamDTO.getMoTa());
        sanPham.setGia(sanPhamDTO.getGia());
        sanPham.setGiaKhuyenMai(sanPhamDTO.getGiaKhuyenMai());
        sanPham.setSoLuong(sanPhamDTO.getSoLuong());
        sanPham.setSoLuongManhGhep(0); // Giá trị mặc định
        sanPham.setSoLuongTon(sanPhamDTO.getSoLuong());
        sanPham.setAnhDaiDien(null); // Giá trị mặc định
        sanPham.setSoLuongVote(0); // Giá trị mặc định
        sanPham.setDanhGiaTrungBinh(0.0); // Giá trị mặc định

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

        if (sanPhamDTO.getKhuyenMaiId() != null) {
            KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
                    .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
            sanPham.setKhuyenMai(khuyenMai);
        }

        return sanPhamRepository.save(sanPham);
    }

    public Page<SanPham> getAllSanPhams(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return sanPhamRepository.findAll(pageable);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching paginated SanPham list: " + e.getMessage(), e);
        }
    }

    public SanPham getSanPhamById(Integer id) {
        try {
            return sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching SanPham by id: " + e.getMessage(), e);
        }
    }

    public SanPham updateSanPham(Integer id, @Valid SanPhamDTO sanPhamDTO) {
        try {
            SanPham sanPham = getSanPhamById(id);
            sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
            sanPham.setMaSanPham(sanPhamDTO.getMaSanPham());
            sanPham.setDoTuoi(sanPhamDTO.getDoTuoi());
            sanPham.setMoTa(sanPhamDTO.getMoTa());
            sanPham.setGia(sanPhamDTO.getGia());
            sanPham.setGiaKhuyenMai(sanPhamDTO.getGiaKhuyenMai());
            sanPham.setSoLuong(sanPhamDTO.getSoLuong());
            sanPham.setSoLuongManhGhep(0); // Giá trị mặc định
            sanPham.setSoLuongTon(sanPhamDTO.getSoLuong());
            sanPham.setAnhDaiDien(null); // Giá trị mặc định
            sanPham.setSoLuongVote(0); // Giá trị mặc định
            sanPham.setDanhGiaTrungBinh(0.0); // Giá trị mặc định

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

            if (sanPhamDTO.getKhuyenMaiId() != null) {
                KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
                        .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
                sanPham.setKhuyenMai(khuyenMai);
            }

            return sanPhamRepository.save(sanPham);
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
        dto.setDanhMucId(sanPham.getDanhMuc() != null ? sanPham.getDanhMuc().getId() : null);
        dto.setBoSuuTapId(sanPham.getBoSuuTap() != null ? sanPham.getBoSuuTap().getId() : null);
        dto.setKhuyenMaiId(sanPham.getKhuyenMai() != null ? sanPham.getKhuyenMai().getId() : null);
        dto.setTrangThai(sanPham.getTrang_thai());
        return dto;
    }

    public SanPhamResponseDTO createSanPhamResponse(SanPhamDTO sanPhamDTO) {
        SanPham sanPham = createSanPham(sanPhamDTO);
        return convertToResponseDTO(sanPham);
    }

    public SanPhamResponseDTO getSanPhamResponseById(Integer id) {
        SanPham sanPham = getSanPhamById(id);
        return convertToResponseDTO(sanPham);
    }

    public List<SanPhamResponseDTO> getAllSanPhamResponses(int page, int size) {
        Page<SanPham> sanPhams = getAllSanPhams(page, size);
        return sanPhams.stream().map(this::convertToResponseDTO).toList();
    }
}
