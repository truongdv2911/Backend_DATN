package com.example.demo.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "Bo_suu_tap")
public class Bo_suu_tap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ten_bo_suu_tap")
    private String ten_bo_suu_tap;
    @Column(name = "mo_ta")
    private String mo_ta;
    @Column(name = "nam_phat_hanh")
    private Integer nam_phat_hanh;
    @Column(name = "ngay_tao")
    private Date ngay_tao;

}
