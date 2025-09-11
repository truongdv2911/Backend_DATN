package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LichSuDoiDiemResponse {
        private Integer id;
        private Integer diemDaDoi;
        private LocalDateTime ngayDoi;
        private String moTa;
        private String maPhieuGiam;
}
