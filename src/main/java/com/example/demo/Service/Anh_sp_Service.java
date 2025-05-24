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

@Service
public class Anh_sp_Service {

    @Autowired
    private Anh_sp_Repo anhSpRepository;

    @Autowired
    private San_pham_Repo sanPhamRepository;

    private final Path uploadDir = Paths.get("uploads");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 M

    public AnhSp createAnhSp(Anh_sp_DTO dto) {
        SanPham sanPham = sanPhamRepository.findById(dto.getSanpham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        AnhSp anh = new AnhSp();
        anh.setUrl(dto.getUrl());
        anh.setMo_ta(dto.getMoTa());
        anh.setThu_tu(dto.getThuTu());
        anh.setAnh_chinh(dto.getAnhChinh());
        anh.setSanPham(sanPham);
        return anhSpRepository.save(anh);
    }

    public List<AnhSp> getAllAnhSp() {
        return anhSpRepository.findAll();
    }

    public AnhSp getAnhSpById(Integer id) {
        return anhSpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
    }

    public AnhSp updateAnhSp(Integer id, Anh_sp_DTO dto) {
        AnhSp anh = getAnhSpById(id);
        anh.setMo_ta(dto.getMoTa());
        anh.setThu_tu(dto.getThuTu());
        anh.setAnh_chinh(dto.getAnhChinh());
        anh.setUrl(dto.getUrl());
        return anhSpRepository.save(anh);
    }

    public void deleteAnhSp(Integer id) {
        anhSpRepository.deleteById(id);
    }

    public AnhSp uploadAndCreateAnhSp(MultipartFile file, String moTa, Integer thuTu, Boolean anhChinh, Integer sanPhamId) {
        try {
            // Kiểm tra kích thước file
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("Kích thước file vượt quá 10 MB");
            }

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Lấy sản phẩm từ database
            SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Xử lý tên file
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Tên file không hợp lệ");
            }

            // Tạo chuỗi ngẫu nhiên 10 ký tự
            String randomString = generateRandomString(10);

            // Định dạng tên file: anh_<random_10_ký_tự>_<tên_ảnh>
            String uniqueFilename = "anh_" + randomString + "_" + originalFilename;

            // Kiểm tra trùng lặp file
            Path destination = uploadDir.resolve(uniqueFilename);
            if (Files.exists(destination)) {
                throw new RuntimeException("File đã tồn tại: " + uniqueFilename);
            }

            // Lưu file vào thư mục
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Tạo URL cho ảnh
            String url = "/api/anhsp/images/" + uniqueFilename;

            // Tạo đối tượng AnhSp và lưu vào database
            AnhSp anhSp = new AnhSp();
            anhSp.setUrl(url);
            anhSp.setMo_ta(moTa);
            anhSp.setThu_tu(thuTu);
            anhSp.setAnh_chinh(anhChinh);
            anhSp.setSanPham(sanPham);

            return anhSpRepository.save(anhSp);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh", e);
        }
    }

    // Hàm tạo chuỗi ngẫu nhiên 10 ký tự
    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
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
}
