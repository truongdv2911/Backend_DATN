package com.example.demo.Service;

import com.example.demo.DTOs.DanhMucDTO;
import com.example.demo.Entity.DanhMuc;
import com.example.demo.Repository.Danh_muc_Repo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Danh_muc_Service {

    @Autowired
    private Danh_muc_Repo danhMucRepo;

    // Create
    public DanhMuc createDanhMuc(@Valid DanhMucDTO danhMucDTO) {
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setTenDanhMuc(danhMucDTO.getTenDanhMuc());
        danhMuc.setMoTa(danhMucDTO.getMoTa());
        return danhMucRepo.save(danhMuc);
    }

    // Read All
    public List<DanhMuc> getAllDanhMuc() {

        return danhMucRepo.findAll();
    }

    // Read One
    public DanhMuc getDanhMucById(Integer id) {
        return danhMucRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("DanhMuc not found with id: " + id));
    }

    // Update
    public DanhMuc updateDanhMuc(Integer id, @Valid DanhMucDTO danhMucDTO) {
        DanhMuc danhMuc = getDanhMucById(id);
        danhMuc.setTenDanhMuc(danhMucDTO.getTenDanhMuc());
        danhMuc.setMoTa(danhMucDTO.getMoTa());
        return danhMucRepo.save(danhMuc);
    }

    // Delete
    public void deleteDanhMuc(Integer id) {
        DanhMuc danhMuc = getDanhMucById(id);
        danhMucRepo.delete(danhMuc);
    }
}
