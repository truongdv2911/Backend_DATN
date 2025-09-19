package com.example.demo.Repository;

import com.example.demo.Entity.HoaDon;
import com.example.demo.Entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    @Query("select o from HoaDon o where o.user.id = :id ")
    List<HoaDon> findByIdUser(Integer id);

    @Query("SELECT h.maHD FROM HoaDon h ORDER BY h.maHD DESC")
    List<String> findTopMaHoaDon(Pageable pageable);


    @Query("SELECT h.trangThai, COUNT(h) FROM HoaDon h GROUP BY h.trangThai")
    List<Object[]> countByTrangThaiGroup();

    @Query(value = """
        SELECT SUM(tong_tien)
        FROM hoa_don hd
        WHERE trang_thai = N'Hoàn tất'
        AND hd.ngay_lap >= :startDate
    AND hd.ngay_lap < DATEADD(DAY, 1, :endDate)
        """, nativeQuery = true)
    BigDecimal doanhThuTheoNgay(LocalDate startDate, LocalDate endDate);

    @Query(value = """
        SELECT phuong_thuc_thanh_toan, SUM(tong_tien)
        FROM hoa_don hd
        WHERE trang_thai = N'Hoàn tất'
        AND hd.ngay_lap >= :startDate
    AND hd.ngay_lap < DATEADD(DAY, 1, :endDate)
        GROUP BY phuong_thuc_thanh_toan
        """, nativeQuery = true)
    List<Object[]> doanhThuTheoPhuongThucTT(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT COUNT(*) AS orders_today\n" +
            "FROM Hoa_don\n" +
            "WHERE CAST(ngay_lap AS date) = CAST(GETDATE() AS date) and trang_thai = N'Hoàn tất';", nativeQuery = true)
    Integer donToday();

    @Query(value = "SELECT\n" +
            "    CASE \n" +
            "        WHEN y.yesterday_count = 0 THEN NULL      -- hoặc 0 nếu muốn tránh chia cho 0\n" +
            "        ELSE ( (t.today_count - y.yesterday_count) * 100.0 / y.yesterday_count )\n" +
            "    END AS growth_percent\n" +
            "FROM (\n" +
            "    SELECT COUNT(*) AS today_count\n" +
            "    FROM Hoa_don\n" +
            "    WHERE CONVERT(date, ngay_lap) = CONVERT(date, GETDATE())  and trang_thai = N'Hoàn tất'\n" +
            ") t\n" +
            "CROSS JOIN (\n" +
            "    SELECT COUNT(*) AS yesterday_count\n" +
            "    FROM Hoa_don\n" +
            "    WHERE CONVERT(date, ngay_lap) = CONVERT(date, DATEADD(day,-1, GETDATE()))  and trang_thai = N'Hoàn tất'\n" +
            ") y;", nativeQuery = true)
    Double tileDon();

    @Query(value = "SELECT SUM(tong_tien) AS revenue_this_month\n" +
            "FROM Hoa_don\n" +
            "WHERE YEAR(ngay_lap) = YEAR(GETDATE())\n" +
            "  AND MONTH(ngay_lap) = MONTH(GETDATE())\n" +
            "  and trang_thai = N'Hoàn tất';", nativeQuery = true)
    BigDecimal doanhThuThang();

    @Query(value = "SELECT \n" +
            "    ( (this_month - last_month) * 100.0 / NULLIF(last_month,0) ) AS growth_percent\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        (SELECT SUM(tong_tien) FROM Hoa_don\n" +
            "         WHERE YEAR(ngay_lap) = YEAR(GETDATE())\n" +
            "           AND MONTH(ngay_lap) = MONTH(GETDATE()) and trang_thai = N'Hoàn tất') AS this_month ,\n" +
            "        (SELECT SUM(tong_tien) FROM Hoa_don\n" +
            "         WHERE YEAR(ngay_lap) = YEAR(DATEADD(month,-1,GETDATE()))\n" +
            "           AND MONTH(ngay_lap) = MONTH(DATEADD(month,-1,GETDATE())) and trang_thai = N'Hoàn tất') AS last_month\n" +
            ") t;", nativeQuery = true)
    Double tileDoanhThu();

    @Query(value = "select top 10 * from Hoa_don where loai_hoa_don = 2 and trang_thai = N'Đang xử lý' ORDER BY ngay_lap Desc;", nativeQuery = true)
    List<HoaDon> findTop10DonHangNew();

    @Query(value = "select top 10 * from Hoa_don where loai_hoa_don = 2 and trang_thai = N'Đã hủy' ORDER BY ngay_lap Desc;", nativeQuery = true)
    List<HoaDon> findTop10DonHangHuy();

    @Query(value = """
    SELECT CAST(ngay_lap AS DATE) AS order_date,
                                  SUM(tong_tien) AS total
                           FROM Hoa_don
                           WHERE ngay_lap >= DATEADD(day, -6, CAST(GETDATE() AS date))
                           GROUP BY CAST(ngay_lap AS DATE)
                           ORDER BY order_date
""", nativeQuery = true)
    List<Object[]> finddoanhTHu7Days();
}
