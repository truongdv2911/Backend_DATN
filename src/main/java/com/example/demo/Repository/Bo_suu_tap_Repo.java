package com.example.demo.Repository;

import com.example.demo.Entity.BoSuuTap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Bo_suu_tap_Repo extends JpaRepository<BoSuuTap, Integer> {
    boolean existsByTenBoSuuTap(@Param("tenBST") String tenBST);
}
