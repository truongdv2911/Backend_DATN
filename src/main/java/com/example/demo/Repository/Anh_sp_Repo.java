package com.example.demo.Repository;

import com.example.demo.Entity.AnhSp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface Anh_sp_Repo extends JpaRepository<AnhSp, Integer> {

    List<AnhSp> findBySanPhamId(Integer sanPhamId);

    List<AnhSp> findBySanPhamIdIn(Collection<Integer> sanPhamIds);
}
