package com.example.demo.Repository;

import com.example.demo.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Phieu_giam_gia_Repo extends JpaRepository<PhieuGiamGia,Integer> {
}
