package com.example.demo.Repository;

import com.example.demo.Entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    boolean existsByTen(@Param("ten") String ten);
}
