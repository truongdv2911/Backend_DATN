package com.example.demo.DTOs;

import com.example.demo.Entity.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOphanHoiDanhGia {
    @NotEmpty(message = "khong de trong nội dung")
    private String noiDung;
    private LocalDateTime ngayPhanHoi;

    private Integer user_id;
}
