package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LyDoHoan {
    private String lyDo;
    private Integer soLan;
    private BigDecimal tongTien;
}
