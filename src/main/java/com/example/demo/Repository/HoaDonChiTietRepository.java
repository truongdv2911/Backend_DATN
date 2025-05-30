package com.example.demo.Repository;
import com.example.demo.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    @Query("select o from HoaDonChiTiet o where o.hd.id = :id ")
    List<HoaDonChiTiet> findByIdOrder(Integer id);
}
