package com.example.demo.Repository;

import com.example.demo.Entity.SanPhamYeuThich;
import com.example.demo.Entity.ViPhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SPYeuThichRepository extends JpaRepository<SanPhamYeuThich, Integer> {
    boolean existsByWishListIdAndSanPhamId(Integer wListId, Integer sanPhamId);
    List<SanPhamYeuThich> findByWishListId(Integer wlId);
    SanPhamYeuThich findByWishListIdAndSanPhamId(Integer wListId, Integer sanPhamId);
}
