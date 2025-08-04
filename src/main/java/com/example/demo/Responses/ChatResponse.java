package com.example.demo.Responses;

import com.example.demo.Entity.SanPham;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String responseType; // "SEARCH", "ADVICE", "SHIPPING", "FAQ", "GENERAL"
    private String message;
    private List<SanPham> products;
}
