package com.example.demo.Repository;

import com.example.demo.Entity.BoSuuTap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Bo_suu_tap_Repo extends JpaRepository<BoSuuTap, Integer> {
    boolean existsByTenBoSuuTap(@Param("tenBST") String tenBST);

    @Query("SELECT b FROM BoSuuTap b WHERE b.isDelete = 1")
    List<BoSuuTap> findAllActive();
}
