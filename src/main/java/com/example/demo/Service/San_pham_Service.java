package com.example.demo.Service;

import com.example.demo.DTOs.SanPhamDTO;

import com.example.demo.Entity.*;
import com.example.demo.Responses.SanPhamResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public interface San_pham_Service {

    SanPham createSanPham(SanPhamDTO sanPhamDTO);
    Page<SanPham> getAllSanPhams(int page, int size);
    SanPham getSanPhamById(Integer id);
    SanPham updateSanPham(Integer id, SanPhamDTO sanPhamDTO);
    void deleteSanPham(Integer id);
    SanPhamResponseDTO convertToResponseDTO(SanPham sanPham);
    SanPhamResponseDTO createSanPhamResponse(SanPhamDTO sanPhamDTO);
    SanPhamResponseDTO updateSanPhamResponse(Integer id, SanPhamDTO sanPhamDTO);
    SanPhamResponseDTO getSanPhamResponseById(Integer id);
    List<SanPhamResponseDTO> getAllSanPhamResponses(int page, int size);
    String generateMaPhieu();
}
