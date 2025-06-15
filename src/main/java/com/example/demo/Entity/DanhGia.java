package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "tieu_de")
    private String tieuDe;
    @Column(name = "text_danh_gia")
    private String textDanhGia;
    @Column(name = "text_phan_hoi")
    private String textPhanHoi;
    @Column(name = "so_sao")
    private Integer soSao;
    @Column(name = "ngay_danh_gia")
    private LocalDateTime ngayDanhGia;
    @Column(name = "ngay_phan_hoi")
    private LocalDateTime ngayPhanHoi;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "nv_id", referencedColumnName = "id")
    private User nv;

    @ManyToOne
    @JoinColumn(name = "hdct_id", referencedColumnName = "id")
    private HoaDonChiTiet dhct;

    @ManyToOne
    @JoinColumn(name = "san_pham_id", referencedColumnName = "id")
    private SanPham sp;

    @OneToMany(mappedBy = "danhGia", cascade = CascadeType.ALL)
    private List<AnhDanhGia> anhFeedbacks = new ArrayList<>();

    @OneToOne(mappedBy = "danhGia", cascade = CascadeType.ALL)
    private VideoDanhGia videoFeedback;
}
