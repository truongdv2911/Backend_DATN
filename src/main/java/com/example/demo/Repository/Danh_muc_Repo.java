package com.example.demo.Repository;

import com.example.demo.Entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Danh_muc_Repo extends JpaRepository<DanhMuc, Integer> {
    boolean existsByTenDanhMuc(@Param("tenDM") String tenDM);
}
