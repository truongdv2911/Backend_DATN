package com.example.demo.Repository;

import com.example.demo.Entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface San_pham_Repo extends JpaRepository<SanPham,Integer> {
}
