package com.example.demo.Responses;

import com.example.demo.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListUserResponse {
    private List<User> listUser;
    private Integer totalPage;
}
