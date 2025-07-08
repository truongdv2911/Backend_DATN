package com.example.demo.Service;

import com.example.demo.DTOs.SanPhamUpdateDTO;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.Responses.AnhResponse;
import com.example.demo.Responses.SanPhamKMResponse;
import com.example.demo.Responses.SanPhamResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class San_pham_Service {
    private final San_pham_Repo sanPhamRepository;
    private final Anh_sp_Repo anhSpRepo;
    private final Danh_muc_Repo danhMucRepository;
    private final Bo_suu_tap_Repo boSuuTapRepository;
    private final Khuyen_mai_Repo khuyenMaiRepository;
    private final KhuyenMaiSanPhamRepository khuyenMaiSanPhamRepository;

    // Phương thức helper tính trạng thái theo số lượng tồn
    private String tinhTrangThaiTheoTonKho(int soLuongTon) {
        return soLuongTon > 0 ? "Đang kinh doanh" : "Hết hàng";
    }

    public SanPhamResponseDTO createSanPham(@Valid SanPhamUpdateDTO sanPhamDTO) {
        String maSanPham = null;
        if (maSanPham == null || maSanPham.isBlank()) {
            int maxTry = 10;
            int count = 0;
            do {
                maSanPham = generateMaPhieu();
                count++;
                if (count > maxTry) {
                    throw new RuntimeException("Không thể sinh mã san pham mới, vui lòng thử lại!");
                }
            } while (sanPhamRepository.existsByMaSanPham(maSanPham));
        } else {
            if (sanPhamRepository.existsByMaSanPham(maSanPham)) {
                throw new RuntimeException("Mã san pham đã tồn tại!");
            }
        }

        SanPham sanPham = new SanPham();
        sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
        sanPham.setMaSanPham(maSanPham);
        sanPham.setDoTuoi(sanPhamDTO.getDoTuoi());
        sanPham.setMoTa(sanPhamDTO.getMoTa());
        sanPham.setGia(sanPhamDTO.getGia());

        BigDecimal giaGoc = sanPhamDTO.getGia();
        BigDecimal giaKhuyenMai = giaGoc; // mặc định bằng giá gốc

//        if (sanPhamDTO.getKhuyenMaiId() != null) {
//            KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
//                    .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
//            if (!"Đang hoạt động".equalsIgnoreCase(khuyenMai.getTrangThai())) {
//                throw new RuntimeException("Khuyến mãi không còn hoạt động và không thể áp dụng.");
//            }
//            sanPham.setKhuyenMai(khuyenMai);
//            if (khuyenMai.getPhanTramGiam() != null && khuyenMai.getPhanTramGiam() > 0) {
//                BigDecimal phanTram = new BigDecimal(khuyenMai.getPhanTramGiam()).divide(BigDecimal.valueOf(100));
//                giaKhuyenMai = giaGoc.subtract(giaGoc.multiply(phanTram));
//            }
//        }

//        sanPham.setGiaKhuyenMai(giaKhuyenMai);
        sanPham.setSoLuongManhGhep(sanPhamDTO.getSoLuongManhGhep());
        sanPham.setSoLuongTon(sanPhamDTO.getSoLuongTon());
        sanPham.setSoLuongVote(0);
        sanPham.setDanhGiaTrungBinh(0.0);

        if (sanPhamDTO.getDanhMucId() != null) {
            DanhMuc danhMuc = danhMucRepository.findById(sanPhamDTO.getDanhMucId())
                    .orElseThrow(() -> new RuntimeException("DanhMuc not found with id: " + sanPhamDTO.getDanhMucId()));
            sanPham.setDanhMuc(danhMuc);
        }

        if (sanPhamDTO.getBoSuuTapId() != null) {
            BoSuuTap boSuuTap = boSuuTapRepository.findById(sanPhamDTO.getBoSuuTapId())
                    .orElseThrow(() -> new RuntimeException("BoSuuTap not found with id: " + sanPhamDTO.getBoSuuTapId()));
            sanPham.setBoSuuTap(boSuuTap);
        }

        // Gán trạng thái theo số lượng tồn
        sanPham.setTrangThai(tinhTrangThaiTheoTonKho(sanPham.getSoLuongTon()));

        
        SanPham savedSanPham = sanPhamRepository.save(sanPham);
        return convertToResponseDTO(savedSanPham);
    }

    public List<SanPhamKMResponse> getSanPhamKhuyenMaiFull() {
        List<Object[]> rows = sanPhamRepository.findSanPhamWithCurrentKhuyenMai(null, null,null
                ,null,null,null,null);
        LocalDateTime now = LocalDateTime.now();
        return rows.stream().map(r -> {
            SanPhamKMResponse dto = new SanPhamKMResponse();
            dto.setId((Integer) r[0]);
            dto.setTenSanPham((String) r[1]);
            dto.setMaSanPham((String) r[2]);
            dto.setDoTuoi((Integer) r[3]);
            dto.setMoTa((String) r[4]);
            dto.setGia((BigDecimal) r[5]);
            dto.setSoLuongManhGhep((Integer) r[6]);
            dto.setSoLuongTon((Integer) r[7]);
            dto.setSoLuongVote((Integer) r[8]);
            dto.setDanhGiaTrungBinh(r[9] != null ? ((Number) r[9]).doubleValue() : null);
            dto.setDanhMucId((Integer) r[10]);
            dto.setBoSuuTapId((Integer) r[11]);
            dto.setTrangThai((String) r[12]);
            dto.setGiaKhuyenMai((BigDecimal) r[13]); // Có thể null
            Double phanTramKM = r[14] != null ? ((BigDecimal) r[14]).doubleValue() : null;
            dto.setPhanTramKhuyenMai(phanTramKM);
            if (phanTramKM == null) {
                dto.setGiaKhuyenMai(dto.getGia());
            }
            // Set trạng thái khuyến mại
            List<KhuyenMaiSanPham> kmspList = khuyenMaiSanPhamRepository.findBySanPham_Id(dto.getId());
            KhuyenMaiSanPham applying = kmspList.stream()
                .filter(kmsp -> {
                    if (kmsp.getKhuyenMai() == null) return false;
                    LocalDateTime start = kmsp.getKhuyenMai().getNgayBatDau();
                    LocalDateTime end = kmsp.getKhuyenMai().getNgayKetThuc();
                    return (now.isEqual(start) || now.isAfter(start)) && (now.isBefore(end) || now.isEqual(end));
                })
                .findFirst().orElse(null);
            if (applying != null) {
                dto.setTrangThaiKM("Đang áp dụng");
            } else {
                KhuyenMaiSanPham newest = kmspList.stream()
                    .filter(kmsp -> kmsp.getKhuyenMai() != null)
                    .max((a, b) -> a.getKhuyenMai().getNgayBatDau().compareTo(b.getKhuyenMai().getNgayBatDau()))
                    .orElse(null);
                if (newest != null) {
                    LocalDateTime ngayBatDau = newest.getKhuyenMai().getNgayBatDau();
                    LocalDateTime ngayKetThuc = newest.getKhuyenMai().getNgayKetThuc();
                    if (now.isBefore(ngayBatDau)) {
                        dto.setTrangThaiKM("Khuyến mại chưa bắt đầu");
                    } else if (now.isAfter(ngayKetThuc)) {
                        dto.setTrangThaiKM("Khuyến mại đã hết hạn");
                    } else {
                        dto.setTrangThaiKM("Đang áp dụng");
                    }
                } else {
                    dto.setTrangThaiKM("Chưa có khuyến mại");
                }
            }
            // Lấy danh sách url ảnh cho sản phẩm
            List<AnhSp> listAnh = anhSpRepo.findBySanPhamId(dto.getId());
            List<AnhResponse> anhUrls = listAnh.stream()
                    .map(anh -> {
                        AnhResponse response = new AnhResponse();
                        response.setUrl(anh.getUrl());
                        response.setAnhChinh(anh.getAnhChinh());
                        return response;
                    })
                    .toList();
            dto.setAnhUrls(anhUrls);
            return dto;
        }).toList();
    }

    public List<SanPhamResponseDTO> getAllSanPhamResponses() {
        List<SanPham> pageSanPhams = sanPhamRepository.findAll();

        List<SanPhamResponseDTO> dtoList = new ArrayList<>();

        for (SanPham sp : pageSanPhams) {
            List<AnhSp> listAnh = anhSpRepo.findBySanPhamId(sp.getId());

            List<AnhResponse> anhUrls = listAnh.stream()
                    .map(anh -> {
                        AnhResponse response = new AnhResponse();
                        response.setUrl(anh.getUrl());
                        response.setAnhChinh(anh.getAnhChinh());
                        return response;
                    })
                    .toList();

            SanPhamResponseDTO dto = new SanPhamResponseDTO();
            dto.setId(sp.getId());
            dto.setTenSanPham(sp.getTenSanPham());
            dto.setMaSanPham(sp.getMaSanPham());
            dto.setDoTuoi(sp.getDoTuoi());
            dto.setMoTa(sp.getMoTa());
            dto.setGia(sp.getGia());
//            dto.setGiaKhuyenMai(sp.getGiaKhuyenMai());
            dto.setSoLuongManhGhep(sp.getSoLuongManhGhep());
            dto.setSoLuongTon(sp.getSoLuongTon());
            dto.setSoLuongVote(sp.getSoLuongVote());
            dto.setDanhGiaTrungBinh(sp.getDanhGiaTrungBinh());
            dto.setDanhMucId(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null);
            dto.setBoSuuTapId(sp.getBoSuuTap() != null ? sp.getBoSuuTap().getId() : null);
//            dto.setKhuyenMaiId(sp.getKhuyenMai() != null ? sp.getKhuyenMai().getId() : null);
            dto.setTrangThai(sp.getTrangThai());
            dto.setAnhUrls(anhUrls);

            dtoList.add(dto);
        }

        return dtoList;
    }

    public SanPham getSanPhamById(Integer id) {
        try {
            return sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching SanPham by id: " + e.getMessage(), e);
        }
    }

    public SanPhamResponseDTO updateSanPham(Integer id, SanPhamUpdateDTO sanPhamDTO) {
        try {
            SanPham sanPham = getSanPhamById(id);
            sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
            sanPham.setDoTuoi(sanPhamDTO.getDoTuoi());
            sanPham.setMoTa(sanPhamDTO.getMoTa());
            sanPham.setGia(sanPhamDTO.getGia());

            BigDecimal giaGoc = sanPhamDTO.getGia();
            BigDecimal giaKhuyenMai = giaGoc; // mặc định bằng giá gốc

//            if (sanPhamDTO.getKhuyenMaiId() != null) {
//                KhuyenMai khuyenMai = khuyenMaiRepository.findById(sanPhamDTO.getKhuyenMaiId())
//                        .orElseThrow(() -> new RuntimeException("KhuyenMai not found with id: " + sanPhamDTO.getKhuyenMaiId()));
//                if (!"Đang hoạt động".equalsIgnoreCase(khuyenMai.getTrangThai())) {
//                    throw new RuntimeException("Khuyến mãi không còn hoạt động và không thể áp dụng.");
//                }
//                sanPham.setKhuyenMai(khuyenMai);

//                if (khuyenMai.getPhanTramGiam() != null && khuyenMai.getPhanTramGiam() > 0) {
//                    BigDecimal phanTram = new BigDecimal(khuyenMai.getPhanTramGiam()).divide(BigDecimal.valueOf(100));
//                    giaKhuyenMai = giaGoc.subtract(giaGoc.multiply(phanTram));
//                }
//            }

//            sanPham.setGiaKhuyenMai(giaKhuyenMai);

            sanPham.setSoLuongManhGhep(sanPhamDTO.getSoLuongManhGhep());
            sanPham.setSoLuongTon(sanPhamDTO.getSoLuongTon());

            if (sanPhamDTO.getDanhMucId() != null) {
                DanhMuc danhMuc = danhMucRepository.findById(sanPhamDTO.getDanhMucId())
                        .orElseThrow(() -> new RuntimeException("DanhMuc not found with id: " + sanPhamDTO.getDanhMucId()));
                sanPham.setDanhMuc(danhMuc);
            }

            if (sanPhamDTO.getBoSuuTapId() != null) {
                BoSuuTap boSuuTap = boSuuTapRepository.findById(sanPhamDTO.getBoSuuTapId())
                        .orElseThrow(() -> new RuntimeException("BoSuuTap not found with id: " + sanPhamDTO.getBoSuuTapId()));
                sanPham.setBoSuuTap(boSuuTap);
            }

            // Gán trạng thái theo số lượng tồn
            sanPham.setTrangThai(tinhTrangThaiTheoTonKho(sanPhamDTO.getSoLuongTon()));

            List<KhuyenMaiSanPham> ds = khuyenMaiSanPhamRepository.findBySanPham_Id(id);
            for (KhuyenMaiSanPham kmsp : ds) {
                double phanTram = kmsp.getKhuyenMai().getPhanTramKhuyenMai();
                BigDecimal giaMoi = sanPham.getGia().multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(phanTram).divide(BigDecimal.valueOf(100))));
                kmsp.setGiaKhuyenMai(giaMoi);
                khuyenMaiSanPhamRepository.save(kmsp);
            }

            SanPham savedSanPham = sanPhamRepository.save(sanPham);
            return convertToResponseDTO(savedSanPham);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating SanPham: " + e.getMessage(), e);
        }
    }

    public void deleteSanPham(Integer id) {
        try {
            SanPham sanPham = getSanPhamById(id);
            if(sanPham.getTrangThai().equals("Ngừng kinh doanh")){
                if (sanPham.getSoLuongTon() == 0){
                    sanPham.setTrangThai("Hết hàng");
                }else{
                    sanPham.setTrangThai("Đang kinh doanh");
                }
            }else{
                sanPham.setTrangThai("Ngừng kinh doanh");
            }
            sanPhamRepository.save(sanPham);
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting SanPham: " + e.getMessage(), e);
        }
    }
    public SanPhamResponseDTO convertToResponseDTO(SanPham sanPham) {
        List<AnhSp> listAnh = anhSpRepo.findBySanPhamId(sanPham.getId());
        List<AnhResponse> anhUrls = listAnh.stream()
                .map(anh -> {
                    AnhResponse response = new AnhResponse();
                    response.setUrl(anh.getUrl());
                    response.setAnhChinh(anh.getAnhChinh());
                    return response;
                })
                .toList();

        SanPhamResponseDTO dto = new SanPhamResponseDTO();
        dto.setId(sanPham.getId());
        dto.setTenSanPham(sanPham.getTenSanPham());
        dto.setMaSanPham(sanPham.getMaSanPham());
        dto.setDoTuoi(sanPham.getDoTuoi());
        dto.setMoTa(sanPham.getMoTa());
        dto.setGia(sanPham.getGia());
        dto.setSoLuongManhGhep(sanPham.getSoLuongManhGhep());
        dto.setSoLuongTon(sanPham.getSoLuongTon());
        dto.setSoLuongVote(sanPham.getSoLuongVote());
        dto.setDanhGiaTrungBinh(sanPham.getDanhGiaTrungBinh());
        dto.setDanhMucId(sanPham.getDanhMuc() != null ? sanPham.getDanhMuc().getId() : null);
        dto.setBoSuuTapId(sanPham.getBoSuuTap() != null ? sanPham.getBoSuuTap().getId() : null);
        dto.setTrangThai(sanPham.getTrangThai());
        dto.setAnhUrls(anhUrls);

        return dto;
    }

    private String generateMaPhieu() {
        Random random = new Random();
        int randomNumber = random.nextInt(900000) + 100000;
        return "SP" + randomNumber;
    }

    public List<SanPhamKMResponse> timKiemSanPham(
            String keyword,
            BigDecimal giaMin,
            BigDecimal giaMax,
            Integer idDanhMuc,
            Integer idBoSuuTap,
            Integer tuoiMin,
            Integer tuoiMax
    ) {
        List<Object[]> rows = sanPhamRepository.findSanPhamWithCurrentKhuyenMai(
                keyword, giaMin, giaMax,
                idDanhMuc, idBoSuuTap,
                tuoiMin, tuoiMax);
        return rows.stream().map(r -> {
            SanPhamKMResponse dto = new SanPhamKMResponse();
            dto.setId((Integer) r[0]);
            dto.setTenSanPham((String) r[1]);
            dto.setMaSanPham((String) r[2]);
            dto.setDoTuoi((Integer) r[3]);
            dto.setMoTa((String) r[4]);
            dto.setGia((BigDecimal) r[5]);
            dto.setSoLuongManhGhep((Integer) r[6]);
            dto.setSoLuongTon((Integer) r[7]);
            dto.setSoLuongVote((Integer) r[8]);
            dto.setDanhGiaTrungBinh(r[9] != null ? ((Number) r[9]).doubleValue() : null);
            dto.setDanhMucId((Integer) r[10]);
            dto.setBoSuuTapId((Integer) r[11]);
            dto.setTrangThai((String) r[12]);
            dto.setGiaKhuyenMai((BigDecimal) r[13]); // Có thể null
            dto.setPhanTramKhuyenMai(
                    r[14] != null ? ((BigDecimal) r[14]).doubleValue() : null
            );
            return dto;
        }).toList();
    }
}
