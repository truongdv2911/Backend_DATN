package com.example.demo.DTOs;

import com.example.demo.Entity.HoaDonChiTiet;
import com.example.demo.Entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOdanhGia {
    private String tieuDe;
    private String textDanhGia;
    private String textPhanHoi;
    @NotNull(message = "Vui lòng điền số sao")
    @Min(value = 1)
    @Max(value = 5)
    private Integer soSao;
    private LocalDateTime ngayDanhGia;
    private LocalDateTime ngayPhanHoi;

    private Integer user_id;

    private Integer nv_id;

    private Integer hdct_id;

    private Integer sp_id;
}
