package com.example.demo.Entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
@Table(name = "Bo_suu_tap")
public class BoSuuTap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
     Integer id;
    @Column(name = "ten_bo_suu_tap")
     String tenBoSuuTap;
    @Column(name = "mo_ta")
     String moTa;
    @Column(name = "nam_phat_hanh")
     Integer namPhatHanh;
    @Column(name = "ngay_tao")
     Date ngayTao;

}
