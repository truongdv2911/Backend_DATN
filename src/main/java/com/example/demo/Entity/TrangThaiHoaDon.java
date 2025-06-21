package com.example.demo.Entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonValue;



@Getter
@AllArgsConstructor

public enum TrangThaiHoaDon {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPING("Shipping"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNING("Returning"),
    REFUNDED("Refunded");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
