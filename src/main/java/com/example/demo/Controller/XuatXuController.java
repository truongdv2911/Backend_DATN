package com.example.demo.Controller;

import com.example.demo.Repository.XuatXuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/lego-store/xuatXu")
public class XuatXuController {
    private final XuatXuRepository xuatXuRepository;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok(xuatXuRepository.findAll());
    }

}
