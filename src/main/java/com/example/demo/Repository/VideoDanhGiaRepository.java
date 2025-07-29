package com.example.demo.Repository;

import com.example.demo.Entity.VideoDanhGia;
import com.example.demo.Responses.AnhResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoDanhGiaRepository extends JpaRepository<VideoDanhGia, Integer> {
    boolean existsByDanhGiaId(Integer danhGiaId);

    VideoDanhGia findByDanhGiaId(Integer id);
}
