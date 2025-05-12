package com.example.demo.Repository;

import com.example.demo.Entity.Gio_hang_chi_tiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Gio_hang_chi_tiet_Repo extends JpaRepository<Gio_hang_chi_tiet, Integer> {
}
