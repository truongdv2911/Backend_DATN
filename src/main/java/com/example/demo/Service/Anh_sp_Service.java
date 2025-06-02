package com.example.demo.Service;

import com.example.demo.DTOs.Anh_sp_DTO;
import com.example.demo.Entity.AnhSp;
import com.example.demo.Entity.SanPham;

import com.example.demo.Repository.Anh_sp_Repo;
import com.example.demo.Repository.San_pham_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public interface Anh_sp_Service {
    AnhSp createAnhSp(Anh_sp_DTO dto);

    List<AnhSp> getAllAnhSp();

    AnhSp getAnhSpById(Integer id);

    AnhSp updateAnhSp(Integer id, MultipartFile file, String moTa, Integer thuTu, Boolean anhChinh, Integer sanPhamId);

    void deleteAnhSp(Integer id);

    AnhSp uploadAndCreateAnhSp(MultipartFile file, String moTa, Integer thuTu, Boolean anhChinh, Integer sanPhamId);

    UrlResource loadImage(String fileName);

    List<AnhSp> getAnhBySanPhamId(Integer sanPhamId);
}
