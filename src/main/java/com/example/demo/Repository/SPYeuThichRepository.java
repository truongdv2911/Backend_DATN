package com.example.demo.Repository;

import com.example.demo.Entity.SanPhamYeuThich;
import com.example.demo.Entity.ViPhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SPYeuThichRepository extends JpaRepository<SanPhamYeuThich, Integer> {
    boolean existsByUserIdAndSanPhamId(Integer userId, Integer sanPhamId);
    List<SanPhamYeuThich> findByUserId(Integer userId);
    SanPhamYeuThich findByUserIdAndSanPhamId(Integer userId, Integer sanPhamId);
}
