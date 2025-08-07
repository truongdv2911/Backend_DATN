package com.example.demo.Repository;

import com.example.demo.Entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<WishList, Integer> {
    Boolean existsByTen(String ten);
}
