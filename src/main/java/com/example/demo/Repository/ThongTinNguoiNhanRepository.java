package com.example.demo.Repository;

import com.example.demo.Entity.ThongTinNguoiNhan;
import com.example.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThongTinNguoiNhanRepository extends JpaRepository<ThongTinNguoiNhan, Integer> {
    @Query("select t from ThongTinNguoiNhan t where t.user.id = :id")
    List<ThongTinNguoiNhan> GetListById(Integer id);

    ThongTinNguoiNhan findByIsMacDinhAndUserId(Integer isMacDinh, Integer user_id);

    List<ThongTinNguoiNhan> findByUser(User user);
}
