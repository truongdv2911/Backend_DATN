package com.example.demo.Responses;

import com.example.demo.Entity.HoaDonChiTiet;
import com.example.demo.Entity.SanPham;
import com.example.demo.Entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DanhGiaResponse {
    private Integer id;
    private String tenKH;
    private String tieuDe;
    private String textDanhGia;
    private String textPhanHoi;
    private Integer soSao;
    private LocalDateTime ngayDanhGia;
    private LocalDateTime ngayPhanHoi;

    private Integer userId;

    private Integer nvId;

    private Integer dhctId;

    private Integer spId;

    private List<AnhResponse> anhUrls;
    private AnhResponse video;
}
