package com.example.demo.Repository;

import com.example.demo.Entity.LichSuDoiDiem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LichSuDoiDiemRepository extends JpaRepository<LichSuDoiDiem, Integer> {
    List<LichSuDoiDiem> findByUserId(Integer userId);
}
