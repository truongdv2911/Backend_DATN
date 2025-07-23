package com.example.demo.Repository;

import com.example.demo.Entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {
    boolean existsByTenIgnoreCase(@Param("ten") String ten);
}
