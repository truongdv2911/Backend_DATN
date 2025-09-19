package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class DashboardStats {
    private Integer user_tong;
    private Double ti_le_tang_User;

    private Integer don_Hang_hom_nay;
    private Double ti_le_tang_DonHang;

    private BigDecimal doanhThuThang;
    private Double ti_Le_tang_DoanhThu;
}
