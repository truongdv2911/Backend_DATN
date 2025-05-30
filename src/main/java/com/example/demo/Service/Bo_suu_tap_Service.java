package com.example.demo.Service;

import com.example.demo.DTOs.BoSuuTapDTO;
import com.example.demo.Entity.BoSuuTap;
import com.example.demo.Repository.Bo_suu_tap_Repo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class Bo_suu_tap_Service {

    @Autowired
    private Bo_suu_tap_Repo boSuuTapRepo;

    // Create
    public BoSuuTap createBoSuuTap(@Valid BoSuuTapDTO boSuuTapDTO) {
        BoSuuTap boSuuTap = new BoSuuTap();
        boSuuTap.setTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap());
        boSuuTap.setMoTa(boSuuTapDTO.getMoTa());
        boSuuTap.setNamPhatHanh(boSuuTapDTO.getNamPhatHanh());
        boSuuTap.setNgayTao(new Date());
        return boSuuTapRepo.save(boSuuTap);
    }

    // Read All
    public List<BoSuuTap> getAllBoSuuTap() {
        return boSuuTapRepo.findAll();
    }

    // Read One
    public BoSuuTap getBoSuuTapById(Integer id) {
        return boSuuTapRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("BoSuuTap not found with id: " + id));
    }

    // Update
    public BoSuuTap updateBoSuuTap(Integer id, @Valid BoSuuTapDTO boSuuTapDTO) {
        BoSuuTap boSuuTap = getBoSuuTapById(id);
        boSuuTap.setTenBoSuuTap(boSuuTapDTO.getTenBoSuuTap());
        boSuuTap.setMoTa(boSuuTapDTO.getMoTa());
        boSuuTap.setNamPhatHanh(boSuuTapDTO.getNamPhatHanh());
        return boSuuTapRepo.save(boSuuTap);
    }

    // Delete
    public void deleteBoSuuTap(Integer id) {
        BoSuuTap boSuuTap = getBoSuuTapById(id);
        boSuuTapRepo.delete(boSuuTap);
    }
}
