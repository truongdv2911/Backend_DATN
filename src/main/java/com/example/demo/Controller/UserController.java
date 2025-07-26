
package com.example.demo.Controller;

import com.example.demo.Component.JwtTokenUntil;
import com.example.demo.DTOs.DTOlogin;
import com.example.demo.DTOs.DTOuser;
import com.example.demo.DTOs.DoiMatKhauRequest;
import com.example.demo.DTOs.UserUpdateDTO;
import com.example.demo.Entity.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.ListUserResponse;
import com.example.demo.Responses.LoginResponse;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.AuthService;
import com.example.demo.Service.GioHangService;
import com.example.demo.Service.UserService;
import com.example.demo.Service.LichSuLogService;
import com.example.demo.Component.ObjectChangeLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/lego-store/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final GioHangService gioHangService;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final JwtTokenUntil jwtTokenUntil;
    private final AuthenticationManager authenticationManager;
    private final LichSuLogService lichSuLogService;


    @GetMapping("/getRole")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser2(@Valid @RequestBody DTOuser user, BindingResult result) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            User user1 = userService.createUser2(user);
            // Log lịch sử tạo mới
            String moTa = "Tạo mới user: " + user1.getTen() + " - ID: " + user1.getId();
            lichSuLogService.saveLog("TẠO MỚI", "User", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(user1);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody DTOlogin dtOlogin, BindingResult result, HttpServletRequest request) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
//                        tạm comment token nhé
            String token = userService.login(dtOlogin);

            //Lưu vào session or localStogare
            HttpSession hs = request.getSession(true);
            hs.setAttribute("username", dtOlogin.getEmail());

            User user = userRepository.findByEmail(dtOlogin.getEmail()).orElseThrow(() ->
                    new RuntimeException("khong tim thay email user"));
            if (user.getTrangThai() != 1) {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "Tài khoản của bạn đã bị BAN"));
            }
            gioHangService.getOrCreateCart(user.getId());

            return ResponseEntity.ok(new LoginResponse(user.getId(), user.getTen(), dtOlogin.getEmail(), user.getRole().getId(), "Dang nhap thanh cong", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, "Sai thông tin đăng nhập"));
        }

    }


    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody DTOuser user, BindingResult result) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            User user1 = userService.createUser(user);
            gioHangService.getOrCreateCart(user1.getId());
            // Log lịch sử tạo mới
            String moTa = "Tạo mới user: " + user1.getTen() + " - ID: " + user1.getId();
            lichSuLogService.saveLog("TẠO MỚI", "User", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(user1);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }
//        @GetMapping("/me")
//        public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
//            HttpSession session = request.getSession(false);
//            if (session != null && session.getAttribute("user") != null) {
//                String email = (String) session.getAttribute("user");
//                return ResponseEntity.ok(new LoginResponse(email, "Người dùng đã đăng nhập"));
//            }
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(null, "Chưa đăng nhập"));

//        }
    //login basic
    //    @PostMapping("/loginBasic")
    //    public ResponseEntity<?> login1(@Valid @RequestBody DTOlogin dtOlogin, BindingResult result){
    //        try{
    //            if (result.hasErrors()){
    //                List<String> listErorrs = result.getFieldErrors().stream().
    //                        map(errors -> errors.getDefaultMessage()).toList();
    //                return ResponseEntity.badRequest().body(listErorrs);
    //            }
    //            String mess = userService.login1(dtOlogin);
    //            return ResponseEntity.ok(mess);
    //        }catch (Exception e){
    //            return ResponseEntity.badRequest().body(new LoginResponse(null,"Sai thong tin dang nhap"));
    //        }
    //    }

    @GetMapping("/paging")
    public ResponseEntity<?> getAll(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        List<User> users = userService.pageUser(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/getTheoRole")
    public ResponseEntity<?> getMembers(
            @RequestParam(value = "roleId", required = false) String roleId
    ) {
        List<User> users = userRepository.pageUser(roleId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id,
                                        @Valid @RequestBody UserUpdateDTO user,
                                        BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            User userOld = userRepository.findById(id).orElseThrow(() -> new RuntimeException("khong tim thay user"));
            // Log sự thay đổi
            String logThayDoi = ObjectChangeLogger.generateChangeLog(userOld, user);
            String moTa = "Cập nhật user ID: " + id + ". Thay đổi: " + logThayDoi;
            lichSuLogService.saveLog("CẬP NHẬT", "User", moTa, lichSuLogService.getCurrentUserId());
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PutMapping("doiMatKhau/{id}")
    public ResponseEntity<?> doiMatKhauNguoiDung(
            @PathVariable Integer id,
            @Valid
            @RequestBody DoiMatKhauRequest request, BindingResult result) {

        if (result.hasErrors()) {
            String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
            return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
        }
        boolean result1 = userService.AdminDoiMK(id, request.getMatKhauMoi());
        if (result1) {
            return ResponseEntity.ok("Đã đổi mật khẩu thành công cho người dùng ID: " + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, "Không tìm thấy người dùng."));
        }
    }

    @GetMapping("/auth/social-login")
    public ResponseEntity<?> social(@RequestParam("login-type") String loginType,
                                    HttpServletRequest request
    ) {
        loginType = loginType.trim().toLowerCase();
        String url = authService.generateAuthUrl(loginType);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/auth/social/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("login-type") String loginType,
            HttpServletRequest request
    ) {
        try {
            Map<String, Object> userInfo = authService.authenticateAndFetchProfile(code, loginType);
            if (userInfo == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "Fail to authenticate"));
            }
            String accountId = "";
            String name = "";
            String email = "";

            if (loginType.trim().equals("google")) {
                accountId = (String) Objects.requireNonNullElse(userInfo.get("sub"), "");
                name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
                email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
            } else if (loginType.trim().equals("facebook")) {
                accountId = (String) Objects.requireNonNullElse(userInfo.get("id"), "");
                name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
                email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
            }

            // Kiểm tra xem user đã tồn tại chưa
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                // Cập nhật thông tin user nếu đã tồn tại
                user = existingUser.get();
                user.setTen(name);
                if (loginType.trim().equals("google")) {
                    user.setGoogleId(accountId);
                } else if (loginType.trim().equals("facebook")) {
                    user.setFacebookId(accountId);
                }
                user = userRepository.save(user);
            } else {
                // Tạo user mới nếu chưa tồn tại
                DTOuser dtoUser = new DTOuser();
                dtoUser.setEmail(email);
                dtoUser.setTen(name);
                dtoUser.setMatKhau(""); // Mật khẩu mặc định cho social login
                dtoUser.setRole_id(3); // Role mặc định cho user thường

                if (loginType.trim().equals("google")) {
                    dtoUser.setGoogleId(accountId);
                } else if (loginType.trim().equals("facebook")) {
                    dtoUser.setFacebookId(accountId);
                }

                user = userService.createUser2(dtoUser);
            }

            // Kiểm tra trạng thái user
            if (user.getTrangThai() != 1) {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "Tài khoản của bạn đã bị BAN"));
            }

            // Tạo giỏ hàng cho user
            gioHangService.getOrCreateCart(user.getId());

            String token = jwtTokenUntil.generationToken(user);

            return ResponseEntity.ok(new LoginResponse(
                    user.getId(),
                    user.getTen(),
                    user.getEmail(),
                    user.getRole().getId(),
                    "Đăng nhập bằng " + loginType + " thành công",
                    token
            ));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse(400, "Lỗi khi xử lý đăng nhập " + loginType + ": " + e.getMessage()));
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        if (email == null || email.isBlank() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, "Email không đúng định dạng"));
        }
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok(new ErrorResponse(200, "OTP đã được gửi."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            boolean isValid = userService.verifyOtp(email, otp);
            if (isValid) {
                return ResponseEntity.ok(new ErrorResponse(200, "OTP đã được xác nhận."));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(400, "OTP sai hoặc đã hết hạn."));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String newPassword) {
        if (email == null || email.isBlank() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, "Email không đúng định dạng"));
        }
        if (newPassword == null || newPassword.isBlank() || !newPassword.matches("^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{6,}$")) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, "Mật khẩu phải tối thiểu 8 ký tự, bao gồm chữ và số, kí tự đặc biệt"));
        }
        try {
            boolean result = userService.resetPassword(email, newPassword);
            if (result) {
                return ResponseEntity.ok(new ErrorResponse(200, "Thay đổi mật khẩu thành công !"));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(500, "lỗi không xác định"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }
}