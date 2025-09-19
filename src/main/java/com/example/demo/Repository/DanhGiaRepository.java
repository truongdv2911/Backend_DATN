package com.example.demo.Repository;

import com.example.demo.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    boolean existsByUserAndSpAndDhct(User idUser, SanPham idSp, HoaDonChiTiet idHDCT);
    List<DanhGia> findBySpId(Integer idSp);
    List<DanhGia> findAllBySpId(Integer spId);
    DanhGia findByVideoFeedbackId(Integer id);

    @Query(value = "select top 10 * from Danh_gia ORDER BY ngay_danh_gia Desc;", nativeQuery = true)
    List<DanhGia> findTop10DanhGiaNew();
}
