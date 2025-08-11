package com.example.demo.Repository;

import com.example.demo.Entity.HoaDon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    @Query("select o from HoaDon o where o.user.id = :id ")
    List<HoaDon> findByIdUser(Integer id);

    @Query("SELECT h.maHD FROM HoaDon h ORDER BY h.maHD DESC")
    List<String> findTopMaHoaDon(Pageable pageable);


    @Query("SELECT h.trangThai, COUNT(h) FROM HoaDon h GROUP BY h.trangThai")
    List<Object[]> countByTrangThaiGroup();

    @Query(value = """
        SELECT SUM(tong_tien)
        FROM hoa_don
        WHERE trang_thai = N'Hoàn tất'
        AND ngay_lap BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    BigDecimal doanhThuTheoNgay(LocalDate startDate, LocalDate endDate);

    @Query(value = """
        SELECT phuong_thuc_thanh_toan, SUM(tong_tien)
        FROM hoa_don
        WHERE trang_thai = N'Hoàn tất'
        AND ngay_lap BETWEEN :startDate AND :endDate
        GROUP BY phuong_thuc_thanh_toan
        """, nativeQuery = true)
    List<Object[]> doanhThuTheoPhuongThucTT(LocalDate startDate, LocalDate endDate);
}
