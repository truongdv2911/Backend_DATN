package com.example.demo.Repository;

import com.example.demo.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("select u from User u")
    Page<User> pageUser(Pageable pageable);

    boolean existsByEmail(String email); // Thêm phương thức này
}
