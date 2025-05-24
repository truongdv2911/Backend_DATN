package com.example.demo.Repository;

import com.example.demo.Entity.Bo_suu_tap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Bo_suu_tap_Repo extends JpaRepository<Bo_suu_tap, Integer> {
}
