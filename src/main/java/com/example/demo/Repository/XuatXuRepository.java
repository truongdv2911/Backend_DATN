package com.example.demo.Repository;

import com.example.demo.Entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {
    boolean existsByTenIgnoreCase(@Param("ten") String ten);

    @Query(value = """
SELECT th.ten_xuat_xu, SUM(ct.so_luong * ct.gia) AS doanh_thu
FROM [dbo].[Hoa_don_chi_tiet] ct
JOIN san_pham sp ON sp.id = ct.san_pham_id
JOIN Xuat_xu th ON th.id = sp.thuong_hieu_id
JOIN hoa_don hd ON hd.id = ct.hoa_don_id
WHERE hd.trang_thai = N'Hoàn tất'
  AND hd.ngay_lap BETWEEN :startDate AND :endDate
GROUP BY th.ten_xuat_xu;
""", nativeQuery = true)
    List<Object[]> doanhThuTheoXuatXu(LocalDate startDate, LocalDate endDate);
}
