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
            dto.setDanhMucId(sp.getDanhMuc().getId());
            dto.setBoSuuTapId(sp.getBoSuuTap().getId());
            dto.setKhuyenMaiId(sp.getKhuyenMai().getId());
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
        dto.setTrangThai(sanPham.getTrangThai());
        return dto;
    }

    public SanPhamResponseDTO createSanPhamResponse(SanPhamDTO sanPhamDTO) {
        SanPham sanPham = createSanPham(sanPhamDTO);
        return convertToResponseDTO(sanPham);
    }

    public SanPhamResponseDTO updateSanPhamResponse(Integer id, SanPhamDTO sanPhamDTO) {
        SanPham sanPham = updateSanPham(id, sanPhamDTO);
        return convertToResponseDTO(sanPham);
    }

    public SanPhamResponseDTO getSanPhamResponseById(Integer id) {
        SanPham sanPham = getSanPhamById(id);
        return convertToResponseDTO(sanPham);
    }

//    public List<SanPhamResponseDTO> getAllSanPhamResponses(int page, int size) {
//        Page<SanPham> sanPhams = sanPhamRepository.getAllSanPhams(page, size);
//        return sanPhams.stream().map(this::convertToResponseDTO).toList();
//    }
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
