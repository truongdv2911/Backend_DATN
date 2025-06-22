package com.example.demo.Repository;

import com.example.demo.Entity.KhuyenMaiSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface KhuyenMaiSanPhamRepository extends JpaRepository<KhuyenMaiSanPham, Integer> {
    List<KhuyenMaiSanPham> findByKhuyenMai_Id(Integer khuyenMaiId);
    List<KhuyenMaiSanPham> findBySanPham_Id(Integer sanPhamId);
    boolean existsBySanPham_IdAndKhuyenMai_Id(Integer sanPhamId, Integer khuyenMaiId);
    @Query("""
    SELECT kmsp FROM KhuyenMaiSanPham kmsp
    WHERE kmsp.giaKhuyenMai = kmsp.sanPham.gia
    AND kmsp.khuyenMai.ngayBatDau <= :now
      AND kmsp.khuyenMai.ngayKetThuc >= :now
""")
    List<KhuyenMaiSanPham> findChuaCapNhatGiaKhuyenMai(@Param("now") LocalDateTime now);

    @Query("SELECT k FROM KhuyenMaiSanPham k")
    List<KhuyenMaiSanPham> findTatCa();

    @Query(value = """
        SELECT kmsp.gia_khuyen_mai
        FROM khuyenMai_sanPham kmsp
        JOIN khuyen_mai km ON km.id = kmsp.id_khuyen_mai
        WHERE kmsp.id_san_pham = :idSP
        AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_ket_thuc;
        """, nativeQuery = true)
    BigDecimal getGiaKM(Integer idSP);
}
