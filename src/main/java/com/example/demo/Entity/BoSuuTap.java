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
     String ten_bo_suu_tap;
    @Column(name = "mo_ta")
     String mo_ta;
    @Column(name = "nam_phat_hanh")
     Integer nam_phat_hanh;
    @Column(name = "ngay_tao")
     Date ngay_tao;

}
