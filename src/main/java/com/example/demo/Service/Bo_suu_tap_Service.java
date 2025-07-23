package com.example.demo.Service;

import com.example.demo.DTOs.BoSuuTapDTO;
import com.example.demo.Entity.BoSuuTap;
import com.example.demo.Repository.Bo_suu_tap_Repo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Bo_suu_tap_Service {

    private final Bo_suu_tap_Repo boSuuTapRepo;


    public BoSuuTap createBoSuuTap(@Valid BoSuuTapDTO boSuuTapDTO) {
        if (boSuuTapRepo.existsByTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap())) {
            throw new RuntimeException("Tên bộ sưu tập đã tồn tại!");
        }
        BoSuuTap boSuuTap = new BoSuuTap();
        boSuuTap.setTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap());
        boSuuTap.setMoTa(boSuuTapDTO.getMoTa());
        boSuuTap.setNamPhatHanh(boSuuTapDTO.getNamPhatHanh());
        boSuuTap.setNgayTao(new Date());
        return boSuuTapRepo.save(boSuuTap);
    }


    public List<BoSuuTap> getAllBoSuuTap() {
        return boSuuTapRepo.findAll();
    }


    public BoSuuTap getBoSuuTapById(Integer id) {
        return boSuuTapRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bộ sưu tập với id: " + id));
    }

    public BoSuuTap updateBoSuuTap(Integer id, @Valid BoSuuTapDTO boSuuTapDTO) {
        BoSuuTap boSuuTap = getBoSuuTapById(id);
        if (!boSuuTap.getTenBoSuuTap().equals(boSuuTapDTO.getTenBoSuuTap())
                && boSuuTapRepo.existsByTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap())) {
            throw new RuntimeException("Tên bộ sưu tập đã tồn tại!");
        }
        boSuuTap.setTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap());
        boSuuTap.setMoTa(boSuuTapDTO.getMoTa());
        boSuuTap.setNamPhatHanh(boSuuTapDTO.getNamPhatHanh());
        return boSuuTapRepo.save(boSuuTap);
    }

    public void deleteBoSuuTap(Integer id) {
        BoSuuTap boSuuTap = getBoSuuTapById(id);
        
        // Kiểm tra xem có sản phẩm nào trong bộ sưu tập còn tồn kho không
        boolean hasProductsInStock = boSuuTap.getSanPhams().stream()
                .anyMatch(sanPham -> {
                    Integer soLuongTon = sanPham.getSoLuongTon();
                    return soLuongTon != null && soLuongTon > 0;
                });
        if (hasProductsInStock) {
            throw new RuntimeException("Không thể xóa bộ sưu tập. Vẫn còn sản phẩm trong kho (soLuongTon > 0). Vui lòng bán hết tất cả sản phẩm trước khi xóa bộ sưu tập.");
        }
        
        boSuuTapRepo.delete(boSuuTap);
    }
}
