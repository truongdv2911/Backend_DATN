package com.example.demo.Repository;

import com.example.demo.Entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Gio_Hang_Repo extends JpaRepository<GioHang, Integer> {
}
