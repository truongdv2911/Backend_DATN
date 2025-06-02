package com.example.demo.Service.IMPL;

import com.example.demo.DTOs.SanPhamDTO;

import com.example.demo.Entity.BoSuuTap;
import com.example.demo.Entity.DanhMuc;
import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.Bo_suu_tap_Repo;
import com.example.demo.Repository.Danh_muc_Repo;
import com.example.demo.Repository.Khuyen_mai_Repo;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Responses.SanPhamResponseDTO;
import com.example.demo.Service.San_pham_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements San_pham_Service {
    private final San_pham_Repo sanPhamRepository;
    private final Danh_muc_Repo danhMucRepository;
    private final Bo_suu_tap_Repo boSuuTapRepository;
    private final Khuyen_mai_Repo khuyenMaiRepository;

    @Override
    public SanPham createSanPham(@Valid SanPhamDTO sanPhamDTO) {
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
        sanPham.setGiaKhuyenMai(sanPhamDTO.getGiaKhuyenMai());
        sanPham.setSoLuong(sanPhamDTO.getSoLuong());
        sanPham.setSoLuongManhGhep(0);
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

        if (sanPhamDTO.getKhuyenMaiId() != null) {
            KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
                    .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
            sanPham.setKhuyenMai(khuyenMai);
        }
        sanPham.setTrangThai(sanPhamDTO.getTrangThai());
        return sanPhamRepository.save(sanPham);
    }

    @Override
    public Page<SanPham> getAllSanPhams(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return sanPhamRepository.findAll(pageable);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching paginated SanPham list: " + e.getMessage(), e);
        }
    }

    @Override
    public SanPham getSanPhamById(Integer id) {
        try {
            return sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching SanPham by id: " + e.getMessage(), e);
        }
    }

    @Override
    public SanPham updateSanPham(Integer id, @Valid SanPhamDTO sanPhamDTO) {
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
            sanPham.setGiaKhuyenMai(sanPhamDTO.getGiaKhuyenMai());
            sanPham.setSoLuong(sanPhamDTO.getSoLuong());
            sanPham.setSoLuongManhGhep(0);
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

            if (sanPhamDTO.getKhuyenMaiId() != null) {
                KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
                        .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
                sanPham.setKhuyenMai(khuyenMai);
            }
            sanPham.setTrangThai(sanPhamDTO.getTrangThai());
            return sanPhamRepository.save(sanPham);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating SanPham: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteSanPham(Integer id) {
        try {
            SanPham sanPham = getSanPhamById(id);
            sanPhamRepository.delete(sanPham);
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting SanPham: " + e.getMessage(), e);
        }
    }

    @Override
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
        dto.setTrangThai(sanPham.getTrangThai());
        return dto;
    }

    @Override
    public SanPhamResponseDTO createSanPhamResponse(SanPhamDTO sanPhamDTO) {
        SanPham sanPham = createSanPham(sanPhamDTO);
        return convertToResponseDTO(sanPham);
    }

    @Override
    public SanPhamResponseDTO updateSanPhamResponse(Integer id, SanPhamDTO sanPhamDTO) {
        SanPham sanPham = updateSanPham(id, sanPhamDTO);
        return convertToResponseDTO(sanPham);
    }

    @Override
    public SanPhamResponseDTO getSanPhamResponseById(Integer id) {
        SanPham sanPham = getSanPhamById(id);
        return convertToResponseDTO(sanPham);
    }

    @Override
    public List<SanPhamResponseDTO> getAllSanPhamResponses(int page, int size) {
        Page<SanPham> sanPhams = getAllSanPhams(page, size);
        return sanPhams.stream().map(this::convertToResponseDTO).toList();
    }

    @Override
    public String generateMaPhieu() {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder("SP");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
