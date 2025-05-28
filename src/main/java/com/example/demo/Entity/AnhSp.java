package com.example.demo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE )
@JsonIgnoreProperties({"sanPham"})
@Table(name = "Anh_sp")
public class AnhSp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Integer id;

    @ManyToOne
    @JoinColumn(name = "san_pham_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_AnhSp_SanPham"))
     SanPham sanPham;

    @Column(name = "url", nullable = false, length = 500)
     String url;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
     String moTa;
    @Column(name = "thu_tu")
     Integer thuTu;
    @Column(name = "anh_chinh")
     Boolean anhChinh;
}
