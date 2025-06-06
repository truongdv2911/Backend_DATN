package com.example.demo.Repository;

import com.example.demo.Entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

public interface GioHangRepo extends JpaRepository<GioHang, Integer> {
    @Query("select g from GioHang g where g.user.id = :id")
    GioHang findByIdUser(Integer id);
}
