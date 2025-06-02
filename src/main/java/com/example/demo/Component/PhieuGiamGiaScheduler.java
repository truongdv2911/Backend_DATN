package com.example.demo.Component;

import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Service.Phieu_giam_gia_Service;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PhieuGiamGiaScheduler {
    private final Phieu_giam_gia_Service phieu_giam_gia_service;
    private final Phieu_giam_gia_Repo phieuGiamGiaRepo;
    // Chạy mỗi ngày lúc 0h đêm
    @Scheduled(cron = "0 0 0 * * *")
    public void updateTrangThaiPhieuGiamGia() {
        LocalDate now = LocalDate.now();

        List<PhieuGiamGia> danhSach = phieuGiamGiaRepo.findAll();
        for (PhieuGiamGia pgg : danhSach) {
            boolean hetHan = pgg.getNgayKetThuc().isBefore(now);
            boolean hetSoLuong = pgg.getSoLuong() <= 0;

            if (hetHan || hetSoLuong) {
                pgg.setTrangThai("Hết hạn"); // hoặc EXPIRED/OUT_OF_STOCK tùy logic
                phieuGiamGiaRepo.save(pgg);
            }
        }
        System.out.println("Đã cập nhật trạng thái phiếu giảm giá vào " + now);
    }
}
