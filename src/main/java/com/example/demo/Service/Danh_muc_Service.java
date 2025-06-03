package com.example.demo.Service;

import com.example.demo.DTOs.DanhMucDTO;
import com.example.demo.Entity.DanhMuc;
import com.example.demo.Repository.Danh_muc_Repo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class   Danh_muc_Service {

    private final Danh_muc_Repo danhMucRepo;


    public DanhMuc createDanhMuc(@Valid DanhMucDTO danhMucDTO) {
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setTenDanhMuc(danhMucDTO.getTenDanhMuc());
        danhMuc.setMoTa(danhMucDTO.getMoTa());
        return danhMucRepo.save(danhMuc);
    }


    public List<DanhMuc> getAllDanhMuc() {
        return danhMucRepo.findAll();
    }


    public DanhMuc getDanhMucById(Integer id) {
        return danhMucRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
    }


    public DanhMuc updateDanhMuc(Integer id, @Valid DanhMucDTO danhMucDTO) {
        DanhMuc danhMuc = getDanhMucById(id);
        danhMuc.setTenDanhMuc(danhMucDTO.getTenDanhMuc());
        danhMuc.setMoTa(danhMucDTO.getMoTa());
        return danhMucRepo.save(danhMuc);
    }


    public void deleteDanhMuc(Integer id) {
        DanhMuc danhMuc = getDanhMucById(id);
        danhMucRepo.delete(danhMuc);
    }
}
