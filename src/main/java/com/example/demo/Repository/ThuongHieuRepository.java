package com.example.demo.Repository;

import com.example.demo.Entity.DanhMuc;
import com.example.demo.Entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    boolean existsByTen(@Param("ten") String ten);

    @Query("SELECT b FROM ThuongHieu b WHERE b.isDelete = 1")
    List<ThuongHieu> findAllActive();
}
