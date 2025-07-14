package com.example.demo.Repository;

import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface Phieu_giam_gia_Repo extends JpaRepository<PhieuGiamGia,Integer> {
    boolean existsByMaPhieu(String maPhieu);
    boolean existsByTenPhieu(@Param("tenPGG") String tenPGG);
    // Thêm hàm lọc theo loại phiếu giảm giá
    List<PhieuGiamGia> findByLoaiPhieuGiam(String loaiPhieuGiam);
    @Query("SELECT k FROM PhieuGiamGia k WHERE k.trangThai <> 'isDelete'")
    List<PhieuGiamGia> findAllPhieuKhongBiXoa();

    @Query("select p from PhieuGiamGia p where p.trangThai like 'active' and p.giaTriToiThieu <= :tamTinh")
    List<PhieuGiamGia> getPGGPhuHop(@Param("tamTinh")BigDecimal tamTinh);

    @Query(value = """
SELECT
    pg.id,
    pg.ma_phieu,
    pg.ten_phieu,
    pg.gia_tri_giam,
    COUNT(DISTINCT hd.id) AS so_luot_su_dung,
    COUNT(DISTINCT hd.user_id) AS so_nguoi_dung_su_dung,
    SUM(hd.tong_tien) AS tong_tien_ban_duoc,
    SUM(hd.so_tien_giam) AS tong_so_tien_giam,
    SUM(hd.tong_tien + hd.so_tien_giam) AS tong_gia_tri_truoc_giam
FROM
    Phieu_giam_gia pg
LEFT JOIN hoa_don hd ON hd.id_phieu_khuyen_mai = pg.id
where pg.id = :idPhieu
GROUP BY
    pg.id, pg.ma_phieu, pg.ten_phieu, pg.gia_tri_giam
ORDER BY
    so_luot_su_dung DESC;
""", nativeQuery = true)
    Object getChiTietPhieu(Integer idPhieu);
}
