package com.example.demo.Repository;

import com.example.demo.DTOs.VidDanhGia;
import com.example.demo.Entity.VidPhieuHoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VidPhieuHoanRepository extends JpaRepository<VidPhieuHoan, Integer> {
    int countByHoanHangId(Integer idPhieuHoan);
}
