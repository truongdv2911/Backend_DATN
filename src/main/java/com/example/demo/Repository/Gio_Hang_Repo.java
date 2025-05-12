package com.example.demo.Repository;

import com.example.demo.Entity.Gio_hang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Gio_Hang_Repo extends JpaRepository<Gio_hang , Integer> {
}
