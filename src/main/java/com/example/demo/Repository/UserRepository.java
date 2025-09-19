package com.example.demo.Repository;

import com.example.demo.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("select u from User u where (:roleId is null or u.role.id = :roleId) and u.trangThai = 1")
    List<User> pageUser(@Param("roleId") String roleId);

    @Query(value = "SELECT COUNT(*) AS total_users\n" +
            "FROM users where trang_thai =1", nativeQuery = true)
    Integer userActive();

    boolean existsByEmail(String email); // Thêm phương thức này

    List<User> findByTenContainingIgnoreCaseOrEmailContainingIgnoreCase(String hoTen, String email);

    @Query(value = """
SELECT TOP 10 kh.id, kh.ten, COUNT(hd.id) AS so_don, SUM(hd.tong_tien) AS tong_tien
FROM Users kh
JOIN hoa_don hd ON hd.user_id = kh.id
WHERE hd.trang_thai = N'Hoàn tất'
  AND hd.ngay_lap >= :startDate
    AND hd.ngay_lap < DATEADD(DAY, 1, :endDate)
GROUP BY kh.id, kh.ten
ORDER BY tong_tien DESC;
""", nativeQuery = true)
    List<Object[]> topKhachHang(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT\n" +
            "    CASE \n" +
            "        WHEN u.total_until_yesterday = 0 THEN NULL  -- hoặc 0 nếu muốn\n" +
            "        ELSE ( t.today_count * 100.0 / u.total_until_yesterday )\n" +
            "    END AS growth_percent\n" +
            "FROM (\n" +
            "    -- Số user mới hôm nay\n" +
            "    SELECT COUNT(*) AS today_count\n" +
            "    FROM users\n" +
            "    WHERE CONVERT(date, ngay_tao) = CONVERT(date, GETDATE())\n" +
            ") t\n" +
            "CROSS JOIN (\n" +
            "    -- Tổng user đã có đến hết ngày hôm qua\n" +
            "    SELECT COUNT(*) AS total_until_yesterday\n" +
            "    FROM users\n" +
            "    WHERE CONVERT(date, ngay_tao) <= CONVERT(date, DATEADD(day, -1, GETDATE()))\n" +
            ") u;", nativeQuery = true)
    Double phanTramTangUser();

    @Query(value = "select top 10 * from Users u where u.trang_thai = 1 and u.role_id = 3 ORDER BY ngay_tao Desc;", nativeQuery = true)
    List<User> findTop10UserNew();
}