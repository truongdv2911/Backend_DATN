package com.example.demo.Responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
//    private String token;
    private Integer id;
    private String ten;
    private String email;
    private String message;
}
