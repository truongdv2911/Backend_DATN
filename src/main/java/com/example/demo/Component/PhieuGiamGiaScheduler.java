package com.example.demo.Component;

import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.KhuyenMaiSanPham;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.KhuyenMaiSanPhamRepository;
import com.example.demo.Repository.Khuyen_mai_Repo;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Service.Phieu_giam_gia_Service;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PhieuGiamGiaScheduler {

    private final Khuyen_mai_Repo khuyenMaiRepository;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;
    private final KhuyenMaiSanPhamRepository kmspRepo;
    private final San_pham_Repo san_pham_repo;
    // Chạy mỗi ngày lúc 0h đêm
    @Scheduled(fixedDelay  = 3000)
    public void updateTrangThaiPhieuGiamGia() {
        LocalDateTime now = LocalDateTime.now();

        List<PhieuGiamGia> danhSach = phieuGiamGiaRepo.findAll();
        for (PhieuGiamGia pgg : danhSach) {
            String trangThaiMoi;
            if (now.isBefore(pgg.getNgayBatDau())) {
                trangThaiMoi = "inactive";
            } else if (pgg.getNgayKetThuc().isBefore(now) || pgg.getSoLuong() <= 0) {
                trangThaiMoi = "expired";
            } else {
                trangThaiMoi = "active";
            }

            if (!trangThaiMoi.equals(pgg.getTrangThai())) {
                pgg.setTrangThai(trangThaiMoi);
                phieuGiamGiaRepo.save(pgg);
            }
        }
        System.out.println("Đã cập nhật trạng thái phiếu giảm giá vào " + now);
    }

    @Scheduled(fixedDelay  = 5000) // chạy mỗi giây
    public void capNhatTrangThaiKhuyenMai() {
        List<KhuyenMai> danhSachKhuyenMai = khuyenMaiRepository.findAllKhuyenMaiKhongBiXoa();
        LocalDateTime now = LocalDateTime.now();

        for (KhuyenMai km : danhSachKhuyenMai) {
            String trangThaiMoi;

            if (now.isBefore(km.getNgayBatDau())) {
                trangThaiMoi = "inactive";
            } else if (now.isAfter(km.getNgayKetThuc())) {
                trangThaiMoi = "expired";
            } else {
                trangThaiMoi = "active";
            }

            if (!trangThaiMoi.equals(km.getTrangThai())) {
                km.setTrangThai(trangThaiMoi);
                khuyenMaiRepository.save(km);
            }
        }
    }

    @Scheduled(fixedDelay  = 1000) // mỗi 1 s
    public void capNhatGiaKhuyenMai() {
       LocalDateTime now = LocalDateTime.now();
        List<KhuyenMaiSanPham> danhSach = kmspRepo.findTatCa();

        for (KhuyenMaiSanPham kmsp : danhSach) {
            KhuyenMai km = kmsp.getKhuyenMai();
            SanPham sp = kmsp.getSanPham();
            if (km == null || sp == null) continue;
            BigDecimal giaGoc = sp.getGia();

            if (now.isAfter(km.getNgayBatDau()) && now.isBefore(km.getNgayKetThuc())) {

                // Đang trong thời gian khuyến mãi
                BigDecimal giaKM = giaGoc
                        .multiply(BigDecimal.ONE.subtract(
                                BigDecimal.valueOf(km.getPhanTramKhuyenMai()).divide(BigDecimal.valueOf(100)))
                        ).setScale(2, RoundingMode.HALF_UP);
                kmsp.setGiaKhuyenMai(giaKM);
                sp.setGiaKM(giaKM);

            } else if (now.isAfter(km.getNgayKetThuc())) {

                // Khuyến mãi đã hết hạn → gán lại giá khuyến mãi bằng giá gốc
                sp.setGiaKM(giaGoc);

            }
            kmspRepo.save(kmsp);
            san_pham_repo.save(sp);
        }
    }
}
