package com.example.demo.Service;

import com.example.demo.Component.JwtTokenUntil;
import com.example.demo.DTOs.DTOlogin;
import com.example.demo.DTOs.DTOuser;
import com.example.demo.DTOs.UserUpdateDTO;
import com.example.demo.Entity.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUntil jwtTokenUntil;
    private final EmailService emailService;

    @Transactional
    public User createUser(DTOuser dtoUser) {
        if (userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new DataIntegrityViolationException("Email da ton tai");
        }
        User user = new User(null, dtoUser.getTen(), dtoUser.getEmail(), dtoUser.getMatKhau(), dtoUser.getSdt()
                , dtoUser.getNgaySinh(), dtoUser.getDiaChi(), 1, dtoUser.getFacebookId(),
                dtoUser.getGoogleId(), roleRepository.findById(3).orElseThrow(() -> new RuntimeException("Khong tim thay role")), 0);

        if (dtoUser.getFacebookId() == null && dtoUser.getGoogleId() == null) {
            String password = user.getMatKhau();
            String encodePass = passwordEncoder.encode(password);
            user.setMatKhau(encodePass);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User createUser2(DTOuser dtoUser) {
        if (userRepository.existsByEmail(dtoUser.getEmail())) {
            throw new DataIntegrityViolationException("Email da ton tai");
        }
        User user = new User(null, dtoUser.getTen(), dtoUser.getEmail(), dtoUser.getMatKhau(), dtoUser.getSdt()
                , dtoUser.getNgaySinh(), dtoUser.getDiaChi(), 1, dtoUser.getFacebookId(),
                dtoUser.getGoogleId(), roleRepository.findById(dtoUser.getRole_id()).orElseThrow(() -> new RuntimeException("khong tim thay id role")), 0);

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

//    //login token
//    public String login1(DTOlogin dtoLogin) {
//        User user = userRepository.findByEmail(dtoLogin.getEmail())
//                .orElseThrow(() -> new BadCredentialsException("Sai thông tin đăng nhập"));
//
//        if (user.getFacebookId() == null && user.getGoogleId() == null) {
//            if (!passwordEncoder.matches(dtoLogin.getMatKhau(), user.getMatKhau())) {
//                throw new BadCredentialsException("Sai thông tin đăng nhập");
//            }
//        }
//
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(dtoLogin.getEmail(), dtoLogin.getMatKhau(), user.getAuthorities());
//
//        authenticationManager.authenticate(authToken);
//
//        return jwtTokenUntil.generationToken(user);
//    }

    public User updateUser(Integer id, UserUpdateDTO dtOuser) throws Exception {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Khong tim thay id user"));
            if (userRepository.existsByEmail(dtOuser.getEmail())
                    && !user.getEmail().equals(dtOuser.getEmail())){
                throw new Exception("Email da ton tai");
            }
            if (userRepository.findById(id).isEmpty()){
                throw new Exception("Khong tim thay id user");
            }
            user.setTen(dtOuser.getTen());
            user.setEmail(dtOuser.getEmail());
            user.setDiaChi(dtOuser.getDiaChi());
            user.setSdt(dtOuser.getSdt());
            user.setNgaySinh(dtOuser.getNgaySinh());
            user.setTrangThai(dtOuser.getTrangThai());
            user.setRole(roleRepository.findById(dtOuser.getRole_id()).orElseThrow(()->
                    new RuntimeException("role khong ton tai")));
            return userRepository.save(user);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public List<User> pageUser(String keyword){
        if (keyword == null || keyword.isBlank()){
            return userRepository.findAll();
        }
        return userRepository.findByTenContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    public Boolean AdminDoiMK(Integer userId, String matKhauMoi){
        Optional<User> optional = userRepository.findById(userId);
        if (optional.isEmpty()){
            return false;
        }
        User user = optional.get();
        user.setMatKhau(passwordEncoder.encode(matKhauMoi));
        userRepository.save(user);
        return true;
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));

        String otp = UUID.randomUUID().toString().substring(0, 6);
        user.setOtp(otp);
        user.setOtpExpirationTime(LocalDateTime.now().plusMinutes(3)); // OTP hết hạn sau 5 phút
        userRepository.save(user);
        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            return false; // OTP không hợp lệ
        }

        if (user.getOtpExpirationTime().isBefore(LocalDateTime.now())) {
            return false; // OTP đã hết hạn
        }
        return true;
    }

    public boolean resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));

        user.setMatKhau(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpirationTime(null);
        userRepository.save(user);
        return true;
    }

}

