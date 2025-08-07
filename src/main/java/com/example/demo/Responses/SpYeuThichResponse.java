package com.example.demo.Responses;

import com.example.demo.Entity.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpYeuThichResponse {
    private Integer id;
    private Integer wishListId;
    private Integer spId;
    private String maSP;
    private String tenSP;
    private BigDecimal gia;
    private BigDecimal giaKM;
    private Integer doTuoi;
    private Integer soLuongTon;
    private Integer danhMucId;
    private String tenDanhMuc;
    private Integer boSuuTapId;
    private String tenBoSuuTap;
    private Integer xuatXuId;
    private String tenXuatXu;
    private Integer thuongHieuId;
    private String tenThuongHieu;
    private List<AnhSp> anhSps;
    private String trangThai;
}
