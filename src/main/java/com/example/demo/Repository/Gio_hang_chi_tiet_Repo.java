package com.example.demo.Repository;

import com.example.demo.Entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Gio_hang_chi_tiet_Repo extends JpaRepository<GioHangChiTiet, Integer> {
}
