package com.example.demo.Filter;

import com.example.demo.Entity.SanPham;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SanPhamSpecification {

    public static Specification<SanPham> filter(
            String keyword,
            BigDecimal giaMin,
            BigDecimal giaMax,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Integer idDanhMuc,
            Integer idBoSuuTap,
            Integer tuoiMin,
            Integer tuoiMax
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                Predicate p1 = cb.like(cb.lower(root.get("tenSanPham")), "%" + keyword.toLowerCase() + "%");
                Predicate p2 = cb.like(cb.lower(root.get("maSanPham")), "%" + keyword.toLowerCase() + "%");
                predicates.add(cb.or(p1, p2));
            }

            if (giaMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("gia"), giaMin));
            }
            if (giaMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("gia"), giaMax));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ngayTao"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("ngayTao"), toDate));
            }

            if (idDanhMuc != null) {
                predicates.add(cb.equal(root.get("danhMuc").get("id"), idDanhMuc));
            }

            if (idBoSuuTap != null) {
                predicates.add(cb.equal(root.get("boSuuTap").get("id"), idBoSuuTap));
            }

            if (tuoiMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("doTuoi"), tuoiMin));
            }
            if (tuoiMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("doTuoi"), tuoiMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
