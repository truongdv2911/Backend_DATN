package com.example.demo.Repository;
import com.example.demo.Entity.HoaDon;
import com.example.demo.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query(value = """
    SELECT hdct.id FROM [dbo].[Hoa_don_chi_tiet] hdct
        JOIN Hoa_don hd on hdct.hoa_don_id = hd.id
        WHERE hd.user_id = :userId
        AND hdct.san_pham_id = :spId
        AND hd.trang_thai NOT IN (N'Đã hủy', N'Đang xử lý')
        ORDER BY hd.ngay_lap DESC
    """, nativeQuery = true)
    List<Integer> findByUserAndSanPhamOrderByDateDesc(
             Integer userId,
             Integer spId
    );

    @Query("SELECT SUM(ct.soLuong) FROM HoaDonChiTiet ct " +
            "WHERE ct.hd.id = :hoaDonId AND ct.sp.id = :sanPhamId")
    Integer getTongSoLuongSanPhamTrongHoaDon(@Param("hoaDonId") Integer hoaDonId,
                                             @Param("sanPhamId") Integer sanPhamId);

    @Query("SELECT hd.gia FROM HoaDonChiTiet hd " +
            "WHERE hd.hd.id = :idHoaDon AND hd.sp.id = :idSanPham")
    BigDecimal getGiaSanPhamTrongHoaDon(@Param("idHoaDon") Integer idHoaDon,
                                        @Param("idSanPham") Integer idSanPham);

    List<HoaDonChiTiet> findByHd(HoaDon hoaDon);
}
