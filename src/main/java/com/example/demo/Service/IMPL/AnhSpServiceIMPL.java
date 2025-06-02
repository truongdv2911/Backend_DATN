package com.example.demo.Service.IMPL;

import com.example.demo.DTOs.Anh_sp_DTO;
import com.example.demo.Entity.AnhSp;
import com.example.demo.Entity.SanPham;
import com.example.demo.Repository.Anh_sp_Repo;
import com.example.demo.Repository.San_pham_Repo;
import com.example.demo.Service.Anh_sp_Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnhSpServiceIMPL implements Anh_sp_Service {

    private final Anh_sp_Repo anhSpRepository;


    private final San_pham_Repo sanPhamRepository;

    private final Path uploadDir = Paths.get("uploads");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Override
    public AnhSp createAnhSp(Anh_sp_DTO dto) {
        SanPham sanPham = sanPhamRepository.findById(dto.getSanpham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        AnhSp anh = new AnhSp();
        anh.setUrl(dto.getUrl());
        anh.setMoTa(dto.getMoTa());
        anh.setThuTu(dto.getThuTu());
        anh.setAnhChinh(dto.getAnhChinh());
        anh.setSanPham(sanPham);
        return anhSpRepository.save(anh);
    }

    @Override
    public List<AnhSp> getAllAnhSp() {
        return anhSpRepository.findAll();
    }

    @Override
    public AnhSp getAnhSpById(Integer id) {
        return anhSpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
    }

    @Override
    public void deleteAnhSp(Integer id) {
        anhSpRepository.deleteById(id);
    }

    @Override
    public AnhSp updateAnhSp(Integer id, MultipartFile file, String moTa, Integer thuTu, Boolean anhChinh, Integer sanPhamId) {
        try {
            AnhSp anhSp = anhSpRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

            if (file != null && !file.isEmpty()) {
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                if (originalFilename.contains("..")) {
                    throw new RuntimeException("Tên file không hợp lệ");
                }
                String randomString = generateRandomString(10);
                String uniqueFilename = "anh_" + randomString + "_" + originalFilename;
                Path filePath = uploadDir.resolve(uniqueFilename);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, file.getBytes());
                anhSp.setUrl(uniqueFilename);
            }

            anhSp.setMoTa(moTa);
            anhSp.setThuTu(thuTu);
            anhSp.setAnhChinh(anhChinh);

            if (sanPhamId != null) {
                SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                anhSp.setSanPham(sanPham);
            }

            return anhSpRepository.save(anhSp);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật ảnh: " + e.getMessage());
        }
    }

    @Override
    public AnhSp uploadAndCreateAnhSp(MultipartFile file, String moTa, Integer thuTu, Boolean anhChinh, Integer sanPhamId) {
        try {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("Kích thước file vượt quá 10 MB");
            }

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Tên file không hợp lệ");
            }

            String randomString = generateRandomString(10);
            String uniqueFilename = "anh_" + randomString + "_" + originalFilename;
            Path destination = uploadDir.resolve(uniqueFilename);
            if (Files.exists(destination)) {
                throw new RuntimeException("File đã tồn tại: " + uniqueFilename);
            }

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            String url = "/api/anhsp/images/" + uniqueFilename;

            AnhSp anhSp = new AnhSp();
            anhSp.setUrl(url);
            anhSp.setMoTa(moTa);
            anhSp.setThuTu(thuTu);
            anhSp.setAnhChinh(anhChinh);
            anhSp.setSanPham(sanPham);

            return anhSpRepository.save(anhSp);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh", e);
        }
    }

    @Override
    public UrlResource loadImage(String fileName) {
        try {
            Path imagePath = uploadDir.resolve(fileName);
            UrlResource resource = new UrlResource(imagePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Không tìm thấy ảnh");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi load ảnh", e);
        }
    }

    @Override
    public List<AnhSp> getAnhBySanPhamId(Integer sanPhamId) {
        return anhSpRepository.findBySanPham_Id(sanPhamId);
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
