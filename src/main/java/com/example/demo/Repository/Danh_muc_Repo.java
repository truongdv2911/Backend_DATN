package com.example.demo.Repository;

import com.example.demo.Entity.Danh_muc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Danh_muc_Repo extends JpaRepository<Danh_muc, Integer> {
}
