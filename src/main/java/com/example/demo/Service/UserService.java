package com.example.demo.Service;

import com.example.demo.Component.JwtTokenUtil;
import com.example.demo.DTOs.DTOlogin;
import com.example.demo.DTOs.DTOuser;
import com.example.demo.Entity.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUntil;

    @Transactional
    public User createUser(DTOuser dtoUser) {
        if (userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new DataIntegrityViolationException("Email da ton tai");
        }
        User user = new User(null, dtoUser.getTen(), dtoUser.getEmail(), dtoUser.getMatKhau(), dtoUser.getSdt()
                , dtoUser.getNgaySinh(), dtoUser.getDiaChi(), 1, dtoUser.getFacebookId(),
                dtoUser.getGoogleId(), roleRepository.findById(2).orElseThrow(() -> new RuntimeException("Khong tim thay role")));

        if (dtoUser.getFacebookId() == null && dtoUser.getGoogleId() == null) {
            String password = user.getMatKhau();
            String encodePass = passwordEncoder.encode(password);
            user.setMatKhau(encodePass);
        }
        return userRepository.save(user);
    }

    public String login(DTOlogin dtOlogin) throws Exception {
        Optional<User> user = userRepository.findByEmail(dtOlogin.getEmail());
        if (user.isEmpty()) {
            throw new Exception("Sai thong tin dang nhap");
        }
        if (user.get().getFacebookId() == null && user.get().getGoogleId() == null) {
            if (!passwordEncoder.matches(dtOlogin.getMatKhau(), user.get().getMatKhau())) {
                throw new BadCredentialsException("Sai thong tin");
            }
        }
        UsernamePasswordAuthenticationToken passwordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                dtOlogin.getEmail(), dtOlogin.getMatKhau(), user.get().getAuthorities()
        );
        authenticationManager.authenticate(passwordAuthenticationToken);
        return jwtTokenUntil.generationToken(user.get());
    }


    //login username - password binh thuong
    public String login1(DTOlogin dtoLogin) {
        User user = userRepository.findByEmail(dtoLogin.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Sai thông tin đăng nhập"));

        if (user.getFacebookId() == null && user.getGoogleId() == null) {
            if (!passwordEncoder.matches(dtoLogin.getMatKhau(), user.getMatKhau())) {
                throw new BadCredentialsException("Sai thông tin đăng nhập");
            }
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(dtoLogin.getEmail(), dtoLogin.getMatKhau(), user.getAuthorities());

        authenticationManager.authenticate(authToken);

        return jwtTokenUntil.generationToken(user);
    }
}

