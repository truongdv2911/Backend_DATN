package com.example.demo.Repository;

import com.example.demo.Entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Page<HoaDon> findByUserId(Integer userId, Pageable pageable);
    Page<HoaDon> findAll(Pageable pageable);

    @Query("""
    SELECT h FROM HoaDon h
    WHERE (:ma IS NULL OR CAST(h.id AS string) LIKE %:ma% OR h.maVanChuyen LIKE %:ma%)
    AND (:trangThai IS NULL OR h.trangThai = :trangThai)
    AND (:phuongThuc IS NULL OR h.phuongThucThanhToan = :phuongThuc)
    AND (:ten IS NULL OR LOWER(h.user.ten) LIKE LOWER(CONCAT('%', :ten, '%')))
    AND (:sdt IS NULL OR h.user.sdt LIKE %:sdt%)
    AND (:from IS NULL OR h.ngayTao >= :from)
    AND (:to IS NULL OR h.ngayTao <= :to)
""")
    Page<HoaDon> searchAdvanced(
            @Param("ma") String ma,
            @Param("trangThai") String trangThai,
            @Param("phuongThuc") String phuongThuc,
            @Param("ten") String tenNguoiDung,
            @Param("sdt") String sdt,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

}
