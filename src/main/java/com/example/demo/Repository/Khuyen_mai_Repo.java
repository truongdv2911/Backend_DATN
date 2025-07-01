package com.example.demo.Repository;

import com.example.demo.Entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Khuyen_mai_Repo extends JpaRepository<KhuyenMai, Integer> {
    boolean existsByMaKhuyenMai(String maKhuyenMai);

    @Query("SELECT k FROM KhuyenMai k WHERE k.trangThai <> 'isDelete'")
    List<KhuyenMai> findAllKhuyenMaiKhongBiXoa();
}