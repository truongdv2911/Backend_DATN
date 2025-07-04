package com.example.demo.Repository;

import com.example.demo.Entity.KhuyenMai;
import com.example.demo.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface Phieu_giam_gia_Repo extends JpaRepository<PhieuGiamGia,Integer> {
    boolean existsByMaPhieu(String maPhieu);

    // Thêm hàm lọc theo loại phiếu giảm giá
    List<PhieuGiamGia> findByLoaiPhieuGiam(String loaiPhieuGiam);
    @Query("SELECT k FROM PhieuGiamGia k WHERE k.trangThai <> 'isDelete'")
    List<PhieuGiamGia> findAllPhieuKhongBiXoa();

    @Query("select p from PhieuGiamGia p where p.trangThai like 'active' and p.giaTriToiThieu <= :tamTinh")
    List<PhieuGiamGia> getPGGPhuHop(@Param("tamTinh")BigDecimal tamTinh);
}
