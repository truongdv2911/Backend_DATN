package com.example.demo.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOlogin {
    @NotEmpty(message = "khong de trong email")
    @Email
    private String email;
    @NotEmpty(message = "Khong de trong mat khau")
    private String matKhau;
}
