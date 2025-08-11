package com.example.demo.Repository;

import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Responses.KhuyenMaiSPResponse;
import com.example.demo.Responses.SanPhamKMResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface Khuyen_mai_Repo extends JpaRepository<KhuyenMai, Integer> {
    boolean existsByMaKhuyenMai(String maKhuyenMai);
    boolean existsByTenKhuyenMai(@Param("tenKM") String tenKM);
    @Query("SELECT k FROM KhuyenMai k WHERE k.trangThai <> 'isDelete'")
    List<KhuyenMai> findAllKhuyenMaiKhongBiXoa();
    @Modifying
    @Transactional
    @Query("DELETE FROM KhuyenMaiSanPham k WHERE k.khuyenMai.id = :idKM")
    void deleteByKhuyenMaiId(@Param("idKM") Integer idKM);

    @Query(value = """
SELECT
    km.id,
    km.ten_khuyen_mai,
    km.phan_tram_khuyen_mai,
    km.ngay_bat_dau,
    km.ngay_ket_thuc,
    COUNT( kmsp.id_san_pham) AS so_san_pham_ap_dung
FROM
    khuyen_mai km
LEFT JOIN khuyenMai_sanPham kmsp ON km.id = kmsp.id_khuyen_mai
LEFT JOIN san_pham sp ON sp.id = kmsp.id_san_pham
    WHERE km.id = :idKM
GROUP BY
    km.id, km.ten_khuyen_mai, km.phan_tram_khuyen_mai,
 km.ngay_bat_dau, km.ngay_ket_thuc;
""", nativeQuery = true)
    Object getDetailKM(Integer idKM);

    @Query(value = """
SELECT
    SUM(hdct.so_luong) AS tong_so_luong_ban,
    SUM(hdct.so_luong * sp.gia) AS tong_tien_truoc_giam,
    SUM(hdct.so_luong * (sp.gia - COALESCE(kmsp.gia_khuyen_mai, 0))) AS  tong_so_tien_giam,
    SUM(hdct.so_luong * COALESCE(kmsp.gia_khuyen_mai, 0)) AS tong_tien_sau_giam,
    COUNT(DISTINCT hd.id) AS so_hoa_don
FROM
    khuyen_mai km
LEFT JOIN khuyenMai_sanPham kmsp ON km.id = kmsp.id_khuyen_mai
LEFT JOIN san_pham sp ON sp.id = kmsp.id_san_pham
LEFT JOIN hoa_don_chi_tiet hdct ON hdct.san_pham_id = sp.id
LEFT JOIN hoa_don hd ON hd.id = hdct.hoa_don_id
    WHERE km.id = :idKM
      AND hd.ngay_lap BETWEEN km.ngay_bat_dau AND km.ngay_ket_thuc
GROUP BY
    km.id, km.ten_khuyen_mai, km.phan_tram_khuyen_mai,
 km.ngay_bat_dau, km.ngay_ket_thuc;
""", nativeQuery = true)
    Object getDetailKMV2(Integer idKM);

    @Query("SELECT \n" +
            "  DISTINCT sp.maSanPham, \n" +
            "    sp.tenSanPham, \n" +
            "    sp.trangThai, \n" +
            "    sp.gia, \n" +
            "  ksp.giaKhuyenMai\n" +
            "FROM \n" +
            "    KhuyenMaiSanPham ksp\n" +
            "JOIN \n" +
            "    SanPham sp ON ksp.sanPham.id = sp.id\n" +
            "WHERE \n" +
            "    ksp.khuyenMai.id = :idKhuyenMai")
    List<Object> getSpInKM(Integer idKhuyenMai);

    @Query(value = """
SELECT
    km.id AS id_khuyen_mai,
    km.ten_khuyen_mai,
    COUNT(DISTINCT hd.id) AS so_don_ap_dung,
    SUM(ct.so_luong * sp.gia) AS tong_doanh_thu_goc,
    SUM(ct.so_luong * ct.gia) AS tong_doanh_thu_sau_giam,
    SUM(ct.so_luong * (sp.gia - ct.gia)) AS tong_tien_giam
FROM khuyen_mai km
JOIN khuyenmai_sanpham ksp ON ksp.id_khuyen_mai = km.id
JOIN [dbo].[Hoa_don_chi_tiet] ct ON ct.san_pham_id = ksp.id_san_pham
JOIN san_pham sp ON sp.id = ct.san_pham_id
JOIN hoa_don hd ON hd.id = ct.hoa_don_id
WHERE hd.trang_thai = N'Hoàn tất'
  AND hd.ngay_lap BETWEEN :startDate AND :endDate
GROUP BY km.id, km.ten_khuyen_mai
ORDER BY tong_tien_giam DESC
""", nativeQuery = true)
    List<Object[]> listKmHieuQua(LocalDate startDate, LocalDate endDate);
}