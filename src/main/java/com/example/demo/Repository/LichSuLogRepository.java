package com.example.demo.Repository;

import com.example.demo.Entity.LichSuLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LichSuLogRepository extends JpaRepository<LichSuLog, Integer> {
    List<LichSuLog> findByBangIgnoreCase(String bang);
}
