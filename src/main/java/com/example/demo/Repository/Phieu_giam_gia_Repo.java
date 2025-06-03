package com.example.demo.Repository;

import com.example.demo.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Phieu_giam_gia_Repo extends JpaRepository<PhieuGiamGia,Integer> {
    boolean existsByMaPhieu(String maPhieu);

    // Thêm hàm lọc theo loại phiếu giảm giá
    List<PhieuGiamGia> findByLoaiPhieuGiam(String loaiPhieuGiam);
}
