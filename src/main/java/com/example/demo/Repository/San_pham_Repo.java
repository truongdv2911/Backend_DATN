package com.example.demo.Repository;

import com.example.demo.Entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface San_pham_Repo extends JpaRepository<SanPham,Integer> {
    boolean existsByMaSanPham(@Param("maSanPham") String maSanPham);

    @EntityGraph(attributePaths = "anhSps")
    Page<SanPham> findAll(Pageable pageable);
}
