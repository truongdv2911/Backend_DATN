package com.example.demo.DTOs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DoiMatKhauRequest {
    @NotEmpty(message = "Khong de trong mat khau")
    @Size(min = 6, max = 30, message = "Mật khẩu phải từ 6 đến 30 ký tự")
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{6,}$",
            message = "Mật khẩu phải có ít nhất 1 ký tự đặc biệt hoặc sai định dạng"
    )
    private String matKhauMoi;
}
