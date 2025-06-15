package com.example.demo.Repository;

import com.example.demo.Entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    boolean existsByUserIdAndSpIdAndAndDhctId(Integer idUser, Integer idSp, Integer idHDCT);
    List<DanhGia> findBySpId(Integer idSp);
}
