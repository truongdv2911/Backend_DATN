package com.example.demo.Service;

import com.example.demo.DTOs.DanhMucDTO;
import com.example.demo.Entity.DanhMuc;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface  Danh_muc_Service {
    DanhMuc createDanhMuc(DanhMucDTO danhMucDTO);

    List<DanhMuc> getAllDanhMuc();

    DanhMuc getDanhMucById(Integer id);

    DanhMuc updateDanhMuc(Integer id, DanhMucDTO danhMucDTO);

    void deleteDanhMuc(Integer id);
}
