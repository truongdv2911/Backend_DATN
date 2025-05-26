package com.example.demo.Service;

import com.example.demo.DTOs.SanPhamDTO;
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

    // Create
    public SanPham createSanPham(@Valid SanPhamDTO sanPhamDTO) {
        SanPham sanPham = new SanPham();
        sanPham.setTen_san_pham(sanPhamDTO.getTenSanPham());
        sanPham.setMa_san_pham(sanPhamDTO.getMaSanPham());
        sanPham.setDo_tuoi(sanPhamDTO.getDoTuoi());
        sanPham.setMo_ta(sanPhamDTO.getMoTa());
        sanPham.setGia(sanPhamDTO.getGia());
        sanPham.setGia_khuyen_mai(sanPhamDTO.getGiaKhuyenMai());
        sanPham.setSo_luong(sanPhamDTO.getSoLuong());
        sanPham.setSo_luong_manh_ghep(sanPhamDTO.getSoLuongManhGhep());
        sanPham.setSo_luong_ton(sanPhamDTO.getSoLuongTon());
        sanPham.setAnh_dai_dien(sanPhamDTO.getAnhDaiDien());
        sanPham.setSo_luong_vote(sanPhamDTO.getSoLuongVote());
        sanPham.setDanh_gia_trung_binh(sanPhamDTO.getDanhGiaTrungBinh());

        // Validate and set DanhMuc
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


    public Page<SanPham> getAllSanPhams(Pageable pageable) {

        return sanPhamRepository.findAll(pageable);
    }


    public SanPham getSanPhamById(Integer id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + id));
    }


    public SanPham updateSanPham(Integer id, @Valid SanPhamDTO sanPhamDTO) {
        SanPham sanPham = getSanPhamById(id);
        sanPham.setTen_san_pham(sanPhamDTO.getTenSanPham());
        sanPham.setMa_san_pham(sanPhamDTO.getMaSanPham());
        sanPham.setDo_tuoi(sanPhamDTO.getDoTuoi());
        sanPham.setMo_ta(sanPhamDTO.getMoTa());
        sanPham.setGia(sanPhamDTO.getGia());
        sanPham.setGia_khuyen_mai(sanPhamDTO.getGiaKhuyenMai());
        sanPham.setSo_luong(sanPhamDTO.getSoLuong());
        sanPham.setSo_luong_manh_ghep(sanPhamDTO.getSoLuongManhGhep());
        sanPham.setSo_luong_ton(sanPhamDTO.getSoLuongTon());
        sanPham.setAnh_dai_dien(sanPhamDTO.getAnhDaiDien());
        sanPham.setSo_luong_vote(sanPhamDTO.getSoLuongVote());
        sanPham.setDanh_gia_trung_binh(sanPhamDTO.getDanhGiaTrungBinh());


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

    // Delete
    public void deleteSanPham(Integer id) {
        SanPham sanPham = getSanPhamById(id);
        sanPhamRepository.delete(sanPham);
    }
}
