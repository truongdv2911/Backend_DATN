package com.example.demo.Service;

import com.example.demo.DTOs.GioHangDTO;
import com.example.demo.Entity.GioHang;
import com.example.demo.Entity.PhieuGiamGia;
import com.example.demo.Entity.User;
import com.example.demo.Repository.Gio_Hang_Repo;
import com.example.demo.Repository.Phieu_giam_gia_Repo;
import com.example.demo.Repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface Gio_hang_Service {

    GioHang createGioHang(GioHangDTO gioHangDTO);

    List<GioHang> getAllGioHangs();

    GioHang getGioHangById(Integer id);

    GioHang updateGioHang(Integer id, GioHangDTO gioHangDTO);

    void deleteGioHang(Integer id);
}
