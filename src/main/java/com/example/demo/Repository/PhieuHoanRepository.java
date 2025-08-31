package com.example.demo.Repository;

import com.example.demo.Entity.HoaDon;
import com.example.demo.Entity.PhieuHoanHang;
import com.example.demo.Enum.TrangThaiPhieuHoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PhieuHoanRepository extends JpaRepository<PhieuHoanHang, Integer> {
    List<PhieuHoanHang> findByTrangThai(TrangThaiPhieuHoan trangThai);

    Optional<PhieuHoanHang> findByHoaDonAndTrangThai(HoaDon hoaDon, TrangThaiPhieuHoan choDuyet);
    List<PhieuHoanHang> findByHoaDon(HoaDon hoaDon);
    List<PhieuHoanHang> findByHoaDonOrderByNgayHoanDesc(HoaDon hoaDon);

    @Query(value = """
SELECT TOP 10 ly_do, COUNT(*) AS so_lan, SUM(tong_tien_hoan) AS tong_tien
FROM phieu_hoan_hang
WHERE ngay_hoan BETWEEN :startDate AND :endDate
GROUP BY ly_do;
""", nativeQuery = true)
    List<Object[]> tongDonBiHoan(LocalDate startDate, LocalDate endDate);

    @Query(value = """
SELECT
    CAST(COUNT(DISTINCT ph.id_hoa_don) AS FLOAT) /\s
    (SELECT COUNT(*) FROM hoa_don WHERE ngay_lap BETWEEN :startDate AND :endDate) * 100 AS ti_le_hoan
FROM phieu_hoan_hang ph
WHERE ph.ngay_hoan >= :startDate
    AND ph.ngay_hoan < DATEADD(DAY, 1, :endDate)
""", nativeQuery = true)
    BigDecimal tyLeHoan(LocalDate startDate, LocalDate endDate);
}
