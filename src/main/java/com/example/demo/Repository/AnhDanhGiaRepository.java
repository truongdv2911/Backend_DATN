package com.example.demo.Repository;

import com.example.demo.Entity.AnhDanhGia;
import com.example.demo.Entity.AnhSp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnhDanhGiaRepository extends JpaRepository<AnhDanhGia, Integer> {
    int countByDanhGiaId(Integer danhGiaId);

    List<AnhDanhGia> findByDanhGiaId(Integer id);
}
