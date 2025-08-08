package com.example.demo.Repository;

import com.example.demo.Entity.HoaDon;
import com.example.demo.Entity.PhieuHoanHang;
import com.example.demo.Enum.TrangThaiPhieuHoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhieuHoanRepository extends JpaRepository<PhieuHoanHang, Integer> {
    List<PhieuHoanHang> findByTrangThai(TrangThaiPhieuHoan trangThai);

    Optional<PhieuHoanHang> findByHoaDonAndTrangThai(HoaDon hoaDon, TrangThaiPhieuHoan choDuyet);
    List<PhieuHoanHang> findByHoaDon(HoaDon hoaDon);
    List<PhieuHoanHang> findByHoaDonOrderByNgayHoanDesc(HoaDon hoaDon);
}
