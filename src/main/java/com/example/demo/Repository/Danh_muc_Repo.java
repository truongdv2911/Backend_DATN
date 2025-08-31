package com.example.demo.Repository;

import com.example.demo.Entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface Danh_muc_Repo extends JpaRepository<DanhMuc, Integer> {
    boolean existsByTenDanhMuc(@Param("tenDM") String tenDM);

    @Query(value = """
SELECT TOP 5 th.ten_danh_muc, SUM(ct.so_luong * ct.gia) AS doanh_thu
FROM [dbo].[Hoa_don_chi_tiet] ct
JOIN san_pham sp ON sp.id = ct.san_pham_id
JOIN Danh_muc th ON th.id = sp.danh_muc_id
JOIN hoa_don hd ON hd.id = ct.hoa_don_id
WHERE hd.trang_thai = N'Hoàn tất'
  AND hd.ngay_lap >= :startDate
    AND hd.ngay_lap < DATEADD(DAY, 1, :endDate)
GROUP BY th.ten_danh_muc;
""", nativeQuery = true)
    List<Object[]> doanhThuTheoDanhMuc(LocalDate startDate, LocalDate endDate);
}
