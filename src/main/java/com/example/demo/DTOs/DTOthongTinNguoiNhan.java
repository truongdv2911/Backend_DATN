package com.example.demo.DTOs;

import com.example.demo.Entity.BaseEntity;
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
public class DTOthongTinNguoiNhan extends BaseEntity {
    @NotEmpty(message = "không để trống tên người nhận")
    private String hoTen;
    @NotEmpty(message = "không để trống số điện thoại người nhận")
    private String sdt;
    @NotEmpty(message = "không để trống đường")
    private String duong;
    @NotEmpty(message = "không để trống xã")
    private String xa;
    @NotEmpty(message = "không để trống huyện")
    private String huyen;
    @NotEmpty(message = "không để trống thành phố")
    private String thanhPho;
    private LocalDateTime ngayTao;
    private Integer isMacDinh;
    private Integer idUser;
}
