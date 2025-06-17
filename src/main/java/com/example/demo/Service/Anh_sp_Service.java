package com.example.demo.Service;

import com.example.demo.DTOs.Anh_sp_DTO;
import com.example.demo.Entity.AnhSp;
import com.example.demo.Entity.SanPham;

import com.example.demo.Repository.Anh_sp_Repo;
import com.example.demo.Repository.San_pham_Repo;
import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class Anh_sp_Service {
    private final Anh_sp_Repo anhSpRepository;


    private final San_pham_Repo sanPhamRepository;

    private final Path uploadDir = Paths.get("uploads");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private final San_pham_Repo san_pham_Repo;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/png", "image/jpeg", "image/jpg", "image/gif");
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

//    public AnhSp createAnhSp(Anh_sp_DTO dto) {
//        SanPham sanPham = sanPhamRepository.findById(dto.getSanpham())
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
//        AnhSp anh = new AnhSp();
//        anh.setUrl(dto.getUrl());
//        anh.setMoTa(dto.getMoTa());
//        anh.setThuTu(dto.getThuTu());
//        anh.setAnhChinh(dto.getAnhChinh());
//        anh.setSanPham(sanPham);
//        return anhSpRepository.save(anh);
//    }


    public List<AnhSp> getAllAnhSp() {
        return anhSpRepository.findAll();
    }


    public AnhSp getAnhSpById(Integer id) {
        return anhSpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
    }


    public void deleteAnhSp(Integer id) {
        anhSpRepository.deleteById(id);
    }


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


    public List<AnhSp> uploadAndCreateAnhSp(MultipartFile[] files, String moTa, Integer thuTu, Boolean anhChinh, Integer sanPhamId) {
        if (files == null || files.length == 0) {
            throw new RuntimeException("Chưa chọn ảnh để upload.");
        }

        try {
            // Kiểm tra thư mục tồn tại
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Lấy sản phẩm
            SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Lấy danh sách ảnh đã có của sản phẩm
            List<AnhSp> existingImages = anhSpRepository.findBySanPhamId(sanPhamId);
            int totalImagesAfterUpload = existingImages.size() + files.length;

            if (totalImagesAfterUpload > 5) {
                throw new RuntimeException("Sản phẩm đã có " + existingImages.size() + " ảnh. "
                        + "Chỉ được upload tối đa 5 ảnh. Chỉ có thể thêm " + (5 - existingImages.size()) + " ảnh nữa.");
            }

            List<AnhSp> anhSpList = new ArrayList<>();
            boolean daSetAnhDaiDien = existingImages.isEmpty(); // Chỉ gán ảnh đại diện nếu chưa có

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];

                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new RuntimeException("Kích thước file vượt quá 10 MB: " + file.getOriginalFilename());
                }

                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                if (originalFilename.contains("..")) {
                    throw new RuntimeException("Tên file không hợp lệ: " + originalFilename);
                }

                // Kiểm tra MIME type
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Chỉ cho phép upload file ảnh (.jpg, .jpeg, .png, .gif)");
                }

                // Kiểm tra đuôi file
                String extension = Strings.getFilenameExtension(originalFilename);
                if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                    throw new IllegalArgumentException("Định dạng file ảnh không hợp lệ: ." + extension);
                }

                String randomString = generateRandomString(10);
                String uniqueFilename = "anh_" + randomString + "_" + originalFilename;
                Path destination = uploadDir.resolve(uniqueFilename);

                if (Files.exists(destination)) {
                    throw new RuntimeException("File đã tồn tại: " + uniqueFilename);
                }

                Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

                String url = uniqueFilename;

                AnhSp anhSp = new AnhSp();
                anhSp.setUrl(url);
                anhSp.setMoTa(moTa);
                anhSp.setThuTu(thuTu != null ? thuTu + i : existingImages.size() + i);
                anhSp.setAnhChinh(anhChinh != null && i == 0 && anhChinh);
                anhSp.setSanPham(sanPham);

                // Gán ảnh đại diện nếu ảnh đầu tiên và chưa có ảnh đại diện
                if ((Boolean.TRUE.equals(anhChinh) || daSetAnhDaiDien) &&
                        (i == 0 && (sanPham.getAnhDaiDien() == null || sanPham.getAnhDaiDien().isEmpty()))) {

                    sanPham.setAnhDaiDien(url);
                    sanPhamRepository.save(sanPham);
                    daSetAnhDaiDien = false;
                }

                anhSpList.add(anhSpRepository.save(anhSp));
            }

            return anhSpList;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload ảnh", e);
        }
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
//    public List<AnhSp> getAnhBySanPhamId(Integer sanPhamId) {
//        return anhSpRepository.findBySanPham_Id(sanPhamId);
//    }

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
