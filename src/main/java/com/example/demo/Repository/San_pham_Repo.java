package com.example.demo.Repository;

import com.example.demo.Entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface San_pham_Repo extends JpaRepository<SanPham,Integer> {
    boolean existsByMaSanPham(@Param("maSanPham") String maSanPham);

    @EntityGraph(attributePaths = "anhSps")
    Page<SanPham> findAll(Pageable pageable);

    @Query(value = """
        WITH sp_km AS (
            SELECT\s
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
                kmsp.gia_khuyen_mai,
                km.phan_tram_khuyen_mai,
                ROW_NUMBER() OVER (
                    PARTITION BY sp.id\s
                    ORDER BY\s
                        CASE\s
                            WHEN km.id IS NOT NULL THEN 0  -- Ưu tiên sản phẩm có khuyến mãi hợp lệ
                            ELSE 1\s
                        END
                ) AS rn
            FROM\s
                san_pham sp
            LEFT JOIN khuyenMai_sanPham kmsp
                ON kmsp.id_san_pham = sp.id
            LEFT JOIN khuyen_mai km\s
                ON km.id = kmsp.id_khuyen_mai
                AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_ket_thuc
        )
        SELECT\s
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
            gia_khuyen_mai,
            phan_tram_khuyen_mai
        FROM sp_km
        WHERE rn = 1
""", nativeQuery = true)
    List<Object[]> findSanPhamWithCurrentKhuyenMai();
}
