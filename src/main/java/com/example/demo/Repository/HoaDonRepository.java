package com.example.demo.Repository;

import com.example.demo.Entity.HoaDon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    @Query("select o from HoaDon o where o.user.id = :id ")
    List<HoaDon> findByIdUser(Integer id);

    @Query("SELECT h.maHD FROM HoaDon h ORDER BY h.maHD DESC")
    List<String> findTopMaHoaDon(Pageable pageable);
}
