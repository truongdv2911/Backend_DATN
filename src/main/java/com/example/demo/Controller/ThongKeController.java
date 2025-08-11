package com.example.demo.Controller;

import com.example.demo.Entity.SanPham;
import com.example.demo.Responses.*;
import com.example.demo.Service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lego-store/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {
    private final ThongKeService thongKeService;

    @GetMapping("/doanh-thu-ngay")
    public BigDecimal doanhThuTheoNgay(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.doanhThuTheoNgay(startDate, endDate);
    }

    @GetMapping("/doanh-thu-phuong-thuc-tt")
    public Map<String, BigDecimal> doanhThuTheoPhuongThucTT(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.getDoanhThuTheoPhuongThucTT(startDate, endDate);
    }

    @GetMapping("/doanh-thu-danh-muc")
    public Map<String, BigDecimal> doanhThuTheoDanhMuc(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.getDoanhThuTheoDanhMuc(startDate, endDate);
    }

    @GetMapping("/khuyen-mai-hieu-qua")
    public List<KhuyenMaiHieuQuaDTO> thongKeKhuyenMai(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.thongKeHieuQua(startDate, endDate);
    }

    @GetMapping("/ly-do-hoan")
    public List<LyDoHoan> thongKeLyDoHoan(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.getTongDonBiHoan(startDate, endDate);
    }

    @GetMapping("/ty-le-hoan")
    public BigDecimal tyLeHoan(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.getTyLeHoan(startDate, endDate);
    }

    @GetMapping("/top-san-pham")
    public List<TopSanPham> topSanPham(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.getTopSanPhamDaBan(startDate, endDate);
    }

    @GetMapping("/san-pham-sap-het-hang")
    public List<SanPhamResponseDTO> sanPhamSapHetHang(
            @RequestParam Integer soLuongCanhBao
    ) {
        return thongKeService.getSanPhamSapHetHang(soLuongCanhBao);
    }

    @GetMapping("/top-khach-hang")
    public List<TopKhachHang> topKhachHang(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.getTopKhachHang(startDate, endDate);
    }

    @GetMapping("/doanh-thu-xuat-xu")
    public Map<String, BigDecimal> doanhThuTheoXuatXu(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return thongKeService.getDoanhThuTheoXuatXu(startDate, endDate);
    }
}
