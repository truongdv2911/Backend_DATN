package com.example.demo.Repository;

import com.example.demo.Entity.ViPhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViPhieuGiamGiaRepository extends JpaRepository<ViPhieuGiamGia, Long> {
    boolean existsByUser_IdAndPhieuGiamGia_Id(Integer userId, Integer phieuId);
    List<ViPhieuGiamGia> findByUser_Id(Integer userId);
}
