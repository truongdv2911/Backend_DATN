package com.example.demo.Service.IMPL;

import com.example.demo.DTOs.BoSuuTapDTO;
import com.example.demo.Entity.BoSuuTap;
import com.example.demo.Repository.Bo_suu_tap_Repo;
import com.example.demo.Service.Bo_suu_tap_Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoSuuTapServiceImpl implements Bo_suu_tap_Service {

    private final Bo_suu_tap_Repo boSuuTapRepo;

    @Override
    public BoSuuTap createBoSuuTap(@Valid BoSuuTapDTO boSuuTapDTO) {
        BoSuuTap boSuuTap = new BoSuuTap();
        boSuuTap.setTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap());
        boSuuTap.setMoTa(boSuuTapDTO.getMoTa());
        boSuuTap.setNamPhatHanh(boSuuTapDTO.getNamPhatHanh());
        boSuuTap.setNgayTao(new Date());
        return boSuuTapRepo.save(boSuuTap);
    }

    @Override
    public List<BoSuuTap> getAllBoSuuTap() {
        return boSuuTapRepo.findAll();
    }

    @Override
    public BoSuuTap getBoSuuTapById(Integer id) {
        return boSuuTapRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bộ sưu tập với id: " + id));
    }

    @Override
    public BoSuuTap updateBoSuuTap(Integer id, @Valid BoSuuTapDTO boSuuTapDTO) {
        BoSuuTap boSuuTap = getBoSuuTapById(id);
        boSuuTap.setTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap());
        boSuuTap.setMoTa(boSuuTapDTO.getMoTa());
        boSuuTap.setNamPhatHanh(boSuuTapDTO.getNamPhatHanh());
        return boSuuTapRepo.save(boSuuTap);
    }

    @Override
    public void deleteBoSuuTap(Integer id) {
        BoSuuTap boSuuTap = getBoSuuTapById(id);
        boSuuTapRepo.delete(boSuuTap);
    }
}
