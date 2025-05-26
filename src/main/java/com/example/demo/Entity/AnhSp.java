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

    @Column(nullable = false, length = 500)
     String url;

    @Column(columnDefinition = "TEXT")
     String mo_ta;

     Integer thu_tu;

     Boolean anh_chinh;
}
