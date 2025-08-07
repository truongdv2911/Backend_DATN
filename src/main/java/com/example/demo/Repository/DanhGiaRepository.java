package com.example.demo.Repository;

import com.example.demo.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    boolean existsByUserAndSpAndDhct(User idUser, SanPham idSp, HoaDonChiTiet idHDCT);
    List<DanhGia> findBySpId(Integer idSp);
    List<DanhGia> findAllBySpId(Integer spId);
    DanhGia findByVideoFeedbackId(Integer id);
}
