package com.example.demo.Service;

import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.*;
import com.example.demo.Responses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ThongKeService {
    private final HoaDonRepository hoaDonRepository;
    private final San_pham_Repo san_pham_repo;
    private final Khuyen_mai_Repo khuyenMaiRepo;
    private final Danh_muc_Repo danh_muc_repo;
    private final XuatXuRepository xuatXuRepository;
    private final PhieuHoanRepository phieuHoanRepository;
    private final UserRepository userRepository;

    public BigDecimal doanhThuTheoNgay(LocalDate startDate, LocalDate endDate){
        return hoaDonRepository.doanhThuTheoNgay(startDate,endDate);
    }

    public Map<String, BigDecimal> getDoanhThuTheoPhuongThucTT(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = hoaDonRepository.doanhThuTheoPhuongThucTT(startDate, endDate);
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : results) {
            String phuongThuc = (String) row[0];
            BigDecimal tongTien = (BigDecimal) row[1];
            map.put(phuongThuc, tongTien);
        }
        return map;
    }

    public Map<String, BigDecimal> getDoanhThuTheoDanhMuc(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = danh_muc_repo.doanhThuTheoDanhMuc(startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    public List<KhuyenMaiHieuQuaDTO> thongKeHieuQua(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = khuyenMaiRepo.listKmHieuQua(startDate, endDate);

        return results.stream().map(row -> new KhuyenMaiHieuQuaDTO(
                ((Number) row[0]).intValue(),             // id_khuyen_mai
                (String) row[1],                           // ten_khuyen_mai
                ((Number) row[2]).intValue(),             // so_don_ap_dung
                row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO, // tong_doanh_thu_goc
                row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO, // tong_doanh_thu_sau_giam
                row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO  // tong_tien_giam
        )).toList();
    }

    // Lấy danh sách thống kê lý do hoàn
    public List<LyDoHoan> getTongDonBiHoan(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = phieuHoanRepository.tongDonBiHoan(startDate, endDate);
        return results.stream()
                .map(row -> new LyDoHoan(
                        (String) row[0],
                        ((Number) row[1]).intValue(),
                        row[2] != null ? ((BigDecimal) row[2]) : BigDecimal.ZERO
                ))
                .collect(Collectors.toList());
    }

    public BigDecimal getTyLeHoan(LocalDate startDate, LocalDate endDate) {
        BigDecimal result = phieuHoanRepository.tyLeHoan(startDate, endDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    public List<TopSanPham> getTopSanPhamDaBan(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = san_pham_repo.findTopDaBan(startDate, endDate);

        return results.stream()
                .map(row -> new TopSanPham(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        ((Number) row[2]).intValue(),
                        row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO
                ))
                .toList();
    }

    public List<SanPhamResponseDTO> getSanPhamSapHetHang(Integer soLuongCanhBao) {
        List<SanPham> sanPhams = san_pham_repo.spSapHetHang(soLuongCanhBao);

        return sanPhams.stream().map(sp -> {
            SanPhamResponseDTO dto = new SanPhamResponseDTO();
            dto.setId(sp.getId());
            dto.setTenSanPham(sp.getTenSanPham());
            dto.setMaSanPham(sp.getMaSanPham());
            dto.setDoTuoi(sp.getDoTuoi());
            dto.setMoTa(sp.getMoTa());
            dto.setGia(sp.getGia());
            dto.setSoLuongManhGhep(sp.getSoLuongManhGhep());
            dto.setSoLuongTon(sp.getSoLuongTon());
            dto.setSoLuongVote(sp.getSoLuongVote());
            dto.setDanhGiaTrungBinh(sp.getDanhGiaTrungBinh());
            dto.setDanhMucId(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null);
            dto.setBoSuuTapId(sp.getBoSuuTap() != null ? sp.getBoSuuTap().getId() : null);
            dto.setXuatXuId(sp.getXuatXu() != null ? sp.getXuatXu().getId() : null);
            dto.setThuongHieuId(sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null);
            dto.setNoiBat(sp.getNoiBat());
            dto.setTrangThai(sp.getTrangThai());

            // Map ảnh
            List<AnhResponse> anhResponses = sp.getAnhSps().stream()
                    .map(anh -> new AnhResponse(anh.getId(), anh.getUrl(), anh.getAnhChinh()))
                    .toList();
            dto.setAnhUrls(anhResponses);

            return dto;
        }).toList();
    }

    public List<TopKhachHang> getTopKhachHang(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = userRepository.topKhachHang(startDate, endDate);

        return results.stream().map(obj -> {
            TopKhachHang dto = new TopKhachHang();
            dto.setId(((Number) obj[0]).intValue()); // ID
            dto.setTen((String) obj[1]);              // Tên
            dto.setSoDon(((Number) obj[2]).intValue());// Số đơn
            dto.setTongTien(obj[3] != null ? new BigDecimal(obj[3].toString()) : BigDecimal.ZERO);
            return dto;
        }).toList();
    }

    public Map<String, BigDecimal> getDoanhThuTheoXuatXu(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = xuatXuRepository.doanhThuTheoXuatXu(startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> new BigDecimal(row[1].toString())
                ));
    }
}
