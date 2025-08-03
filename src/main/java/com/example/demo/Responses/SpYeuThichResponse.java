package com.example.demo.Responses;

import com.example.demo.Entity.AnhSp;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpYeuThichResponse {
    private Integer id;
    private Integer userId;
    private Integer spId;
    private String maSP;
    private String tenSP;
    private BigDecimal gia;
    private Integer doTuoi;
    private Integer soLuongTon;
    private List<AnhSp> anhSps;
    private String trangThai;
}
