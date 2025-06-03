package com.example.demo.Repository;

import com.example.demo.Entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Khuyen_mai_Repo extends JpaRepository<KhuyenMai, Integer> {
    boolean existsByMaKhuyenMai(String maKhuyenMai);
}