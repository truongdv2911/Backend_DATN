package com.example.demo.Repository;

import com.example.demo.DTOs.SearchRequestDTO;
import com.example.demo.Entity.SanPham;
import com.example.demo.Responses.SanPhamResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface San_pham_Repo extends JpaRepository<SanPham,Integer>, JpaSpecificationExecutor<SanPham> {
    boolean existsByMaSanPham(@Param("maSanPham") String maSanPham);
    boolean existsByTenSanPham(@Param("tenSanPham") String tenSanPham);

    @Query(value = "select * from San_pham", nativeQuery = true)
    List<SanPham> findAll();

    @Query(value = """
        WITH sp_km AS (
            SELECT
                sp.id AS sp_id,
                sp.ten_san_pham,
                sp.ma_san_pham,
                sp.do_tuoi,
                sp.mo_ta,
                sp.gia,
                sp.so_luong_manh_ghep,
                sp.so_luong_ton,
                sp.so_luong_vote,
                sp.danh_gia_trung_binh,
                sp.danh_muc_id,
                sp.bo_suu_tap_id,
                sp.trang_thai,
                sp.gia_km,
                km.phan_tram_khuyen_mai,
                sp.thuong_hieu_id,
                sp.xuat_xu_id,
                sp.is_noi_bat,
                ROW_NUMBER() OVER (
                    PARTITION BY sp.id
                    ORDER BY
                        CASE
                            WHEN km.id IS NOT NULL THEN 0
                            ELSE 1
                        END
                ) AS rn
            FROM
                san_pham sp
            LEFT JOIN khuyenMai_sanPham kmsp
                ON kmsp.id_san_pham = sp.id
            LEFT JOIN khuyen_mai km
                ON km.id = kmsp.id_khuyen_mai
                AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_ket_thuc
                AND km.trang_thai IN ('ACTIVE', 'INACTIVE')  -- ✅ Điều kiện mới
        )
        SELECT
            sp_id AS id,
            ten_san_pham,
            ma_san_pham,
            do_tuoi,
            mo_ta,
            gia,
            so_luong_manh_ghep,
            so_luong_ton,
            so_luong_vote,
            danh_gia_trung_binh,
            danh_muc_id,
            bo_suu_tap_id,
            trang_thai,
            gia_km,
            phan_tram_khuyen_mai,
            thuong_hieu_id,
            xuat_xu_id,
            is_noi_bat
        FROM sp_km
        WHERE rn = 1
          AND (trang_thai LIKE N'Đang kinh doanh' OR trang_thai LIKE N'Hết hàng')
        AND (:keyword IS NULL OR LOWER(ten_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(ma_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:giaMin IS NULL OR gia >= :giaMin)
                  AND (:giaMax IS NULL OR gia <= :giaMax)
                  AND (:idDanhMuc IS NULL OR danh_muc_id = :idDanhMuc)
                  AND (:idBoSuuTap IS NULL OR bo_suu_tap_id = :idBoSuuTap)
                  AND (:tuoiMin IS NULL OR do_tuoi >= :tuoiMin)
                  AND (:tuoiMax IS NULL OR do_tuoi <= :tuoiMax)
""", nativeQuery = true)
    List<Object[]> findSanPhamWithCurrentKhuyenMai(
            @Param("keyword") String keyword,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            @Param("idDanhMuc") Integer idDanhMuc,
            @Param("idBoSuuTap") Integer idBoSuuTap,
            @Param("tuoiMin") Integer tuoiMin,
            @Param("tuoiMax") Integer tuoiMax
    );

    @Query(value = """
        WITH sp_km AS (
            SELECT
                sp.id AS sp_id,
                sp.ten_san_pham,
                sp.ma_san_pham,
                sp.do_tuoi,
                sp.mo_ta,
                sp.gia,
                sp.so_luong_manh_ghep,
                sp.so_luong_ton,
                sp.so_luong_vote,
                sp.danh_gia_trung_binh,
                sp.danh_muc_id,
                sp.bo_suu_tap_id,
                sp.trang_thai,
                sp.gia_km,
                km.phan_tram_khuyen_mai,
                sp.thuong_hieu_id,
                sp.xuat_xu_id,
                sp.is_noi_bat,
                ROW_NUMBER() OVER (
                    PARTITION BY sp.id
                    ORDER BY
                        CASE
                            WHEN km.id IS NOT NULL THEN 0
                            ELSE 1
                        END
                ) AS rn
            FROM
                san_pham sp
            LEFT JOIN khuyenMai_sanPham kmsp
                ON kmsp.id_san_pham = sp.id
            LEFT JOIN khuyen_mai km
                ON km.id = kmsp.id_khuyen_mai
                AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_ket_thuc
                AND km.trang_thai IN ('ACTIVE', 'INACTIVE')  -- ✅ Điều kiện mới
        )
        SELECT
            sp_id AS id,
            ten_san_pham,
            ma_san_pham,
            do_tuoi,
            mo_ta,
            gia,
            so_luong_manh_ghep,
            so_luong_ton,
            so_luong_vote,
            danh_gia_trung_binh,
            danh_muc_id,
            bo_suu_tap_id,
            trang_thai,
            gia_km,
            phan_tram_khuyen_mai,
            thuong_hieu_id,
            xuat_xu_id,
            is_noi_bat
        FROM sp_km
        WHERE rn = 1
        AND (:keyword IS NULL OR LOWER(ten_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(ma_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:giaMin IS NULL OR gia >= :giaMin)
                  AND (:giaMax IS NULL OR gia <= :giaMax)
                  AND (:idDanhMuc IS NULL OR danh_muc_id = :idDanhMuc)
                  AND (:idBoSuuTap IS NULL OR bo_suu_tap_id = :idBoSuuTap)
                  AND (:tuoiMin IS NULL OR do_tuoi >= :tuoiMin)
                  AND (:tuoiMax IS NULL OR do_tuoi <= :tuoiMax)
""", nativeQuery = true)
    List<Object[]> findSanPhamWithCurrentKhuyenMaiV1(
            @Param("keyword") String keyword,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            @Param("idDanhMuc") Integer idDanhMuc,
            @Param("idBoSuuTap") Integer idBoSuuTap,
            @Param("tuoiMin") Integer tuoiMin,
            @Param("tuoiMax") Integer tuoiMax
    );

    @Query("""
    SELECT sp
    FROM SanPham sp
    JOIN sp.xuatXu xx
    JOIN sp.thuongHieu th
    JOIN sp.boSuuTap bst
    WHERE (:#{#req.doTuoi} IS NULL OR sp.doTuoi <= :#{#req.doTuoi})
       AND (:#{#req.ten} IS NULL OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :#{#req.ten}, '%')))
       AND (:#{#req.gia} IS NULL OR sp.gia <= :#{#req.gia})
       AND (:#{#req.xuatXu} IS NULL OR LOWER(xx.ten) LIKE LOWER(CONCAT('%', :#{#req.xuatXu}, '%')))
       AND (:#{#req.thuongHieu} IS NULL OR LOWER(th.ten) LIKE LOWER(CONCAT('%', :#{#req.thuongHieu}, '%')))
       AND (:#{#req.boSuuTap} IS NULL OR LOWER(bst.tenBoSuuTap) LIKE LOWER(CONCAT('%', :#{#req.boSuuTap}, '%')))
       AND (:#{#req.soLuongManhGhepMin} IS NULL OR sp.soLuongManhGhep >= :#{#req.soLuongManhGhepMin})
       AND (:#{#req.danhGiaToiThieu} IS NULL OR sp.danhGiaTrungBinh >= :#{#req.danhGiaToiThieu})
""")
    List<SanPham> timKiemTheoDieuKien(@Param("req") SearchRequestDTO request);

    @Query(value = """
    SELECT sp 
    FROM san_pham sp
    JOIN chi_tiet_hoa_don cthd ON sp.id = cthd.san_pham_id
    JOIN hoa_don hd ON cthd.hoa_don_id = hd.id
    WHERE hd.trang_thai = N'Hoàn tất'
    GROUP BY sp.id, sp.ten, sp.gia
    ORDER BY da_ban DESC
    LIMIT 3
""", nativeQuery = true)
    List<SanPham> findTopDaBan();
}
