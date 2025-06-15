package com.example.demo.Repository;
import com.example.demo.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    @Query("select o from HoaDonChiTiet o where o.hd.id = :id ")
    List<HoaDonChiTiet> findByIdOrder(Integer id);

    @Query("""
    SELECT CASE WHEN COUNT(hdct) > 0 THEN true ELSE false END
    FROM HoaDonChiTiet hdct
    JOIN HoaDon hd ON hd.id = hdct.hd.id
    WHERE hd.user.id = :userId
      AND hdct.sp.id = :sanPhamId
""")
    boolean hasUserPurchasedSanPham(Integer userId, Integer sanPhamId);
}
