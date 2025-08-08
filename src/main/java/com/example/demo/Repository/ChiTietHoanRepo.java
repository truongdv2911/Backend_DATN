package com.example.demo.Repository;

import com.example.demo.Entity.ChiTietHoanHang;
import com.example.demo.Entity.PhieuHoanHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChiTietHoanRepo extends JpaRepository<ChiTietHoanHang, Integer> {
    List<ChiTietHoanHang> findByPhieuHoanHang(PhieuHoanHang phieuHoanHang);

    @Query("SELECT COALESCE(SUM(ct.soLuongHoan), 0) FROM ChiTietHoanHang ct " +
            "JOIN ct.phieuHoanHang p " +
            "WHERE p.hoaDon.id = :idHoaDon AND ct.sanPham.id = :idSanPham " +
            "AND p.trangThai IN ('DA_DUYET')")
    Integer getTongSoLuongDaHoanTheoSanPham(@Param("idHoaDon") Integer idHoaDon,
                                            @Param("idSanPham") Integer idSanPham);
}
