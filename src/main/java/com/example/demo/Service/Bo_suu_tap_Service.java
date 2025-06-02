package com.example.demo.Service;

import com.example.demo.DTOs.BoSuuTapDTO;
import com.example.demo.Entity.BoSuuTap;
import com.example.demo.Repository.Bo_suu_tap_Repo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface Bo_suu_tap_Service {
    BoSuuTap createBoSuuTap(BoSuuTapDTO boSuuTapDTO);

    List<BoSuuTap> getAllBoSuuTap();

    BoSuuTap getBoSuuTapById(Integer id);

    BoSuuTap updateBoSuuTap(Integer id, BoSuuTapDTO boSuuTapDTO);

    void deleteBoSuuTap(Integer id);
}
