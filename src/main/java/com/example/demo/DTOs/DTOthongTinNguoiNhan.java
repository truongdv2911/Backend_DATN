package com.example.demo.DTOs;

import jakarta.persistence.Column;
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
public class DTOthongTinNguoiNhan {
    @NotEmpty(message = "không để trống tên")
    private String hoTen;
    @NotEmpty(message = "không để trống số điện thoại")
    private String sdt;
    private String duong;
    private String xa;
    private String huyen;
    private String thanhPho;
    private LocalDateTime ngayTao;
    private Integer isMacDinh;
}
