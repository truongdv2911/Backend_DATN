package com.example.demo.Repository;

import com.example.demo.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("select u from User u where (:roleId is null or u.role.id = :roleId) and u.trangThai = 1")
    List<User> pageUser(@Param("roleId") String roleId);

    boolean existsByEmail(String email); // Thêm phương thức này

    List<User> findByTenContainingIgnoreCaseOrEmailContainingIgnoreCase(String hoTen, String email);

}
