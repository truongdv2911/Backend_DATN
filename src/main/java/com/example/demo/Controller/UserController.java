
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
import com.example.demo.Service.AuthService;
import com.example.demo.Service.GioHangService;
import com.example.demo.Service.UserService;
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


//    @GetMapping("/success")
//    public ResponseEntity<?> handleGoogleLogin(HttpServletRequest request) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
//            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//            String email = oAuth2User.getAttribute("email");
//            String name = oAuth2User.getAttribute("name");
//
//            // Kiểm tra hoặc tạo người dùng trong database
//            DTOuser dtoUser = new DTOuser();
//            dtoUser.setEmail(email);
//            dtoUser.setTen(name);
//            dtoUser.setMatKhau("123456@");
//            // Bạn có thể thêm các thuộc tính khác nếu cần
//
//            try {
//                Optional<User> user = userRepository.findByEmail(email);
//                if (user.isEmpty()) {
//                    // Tạo người dùng mới nếu chưa tồn tại
//                    userService.createUser(dtoUser);
//                }
//                // Lưu thông tin vào session
//                HttpSession session = request.getSession(true);
//                session.setAttribute("username", email);
//                return ResponseEntity.ok(new LoginResponse(user.get().getId(), user.get().getTen(), user.get().getEmail(), user.get().getRole().getId(), "Đăng nhập bằng Google thành công"));
//            } catch (Exception e) {
//                return ResponseEntity.badRequest().body(new LoginResponse(null, null,null,null, "Lỗi khi xử lý đăng nhập Google"));
//            }
//        }
//        return ResponseEntity.badRequest().body(new LoginResponse(null, null,null,null, "Xác thực không hợp lệ"));
//    }

    @GetMapping("/getRole")
    public ResponseEntity<?> getRoles(){
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser2(@Valid @RequestBody DTOuser user, BindingResult result){
        try {
            if (result.hasErrors()){
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            User user1 = userService.createUser2(user);
            return ResponseEntity.ok(user1);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody DTOlogin dtOlogin, BindingResult result, HttpServletRequest request) {
        try {
            if (result.hasErrors()) {
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
//                        tạm comment token nhé
                        String token = userService.login(dtOlogin);

            //Lưu vào session or localStogare
            HttpSession hs= request.getSession(true);
            hs.setAttribute("username", dtOlogin.getEmail());

            User user = userRepository.findByEmail(dtOlogin.getEmail()).orElseThrow(()->
                    new RuntimeException("khong tim thay email user"));
            if (user.getTrangThai() != 1){
                return ResponseEntity.badRequest().body("Tài khoản của bạn đã bị BAN");
            }
            gioHangService.getOrCreateCart(user.getId());

            return ResponseEntity.ok(new LoginResponse(user.getId(), user.getTen(), dtOlogin.getEmail(), user.getRole().getId(), "Dang nhap thanh cong",token));
        }    catch (Exception e){
            return ResponseEntity.badRequest().body("Sai thong tin dang nhap");
        }

    }


    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody DTOuser user, BindingResult result){
        try {
            if (result.hasErrors()){
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            User user1 = userService.createUser(user);
            gioHangService.getOrCreateCart(user1.getId());
            return ResponseEntity.ok(user1);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
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
    ){
        List<User> users = userService.pageUser(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/getTheoRole")
    public ResponseEntity<?> getMembers(
            @RequestParam(value = "roleId", required = false) String roleId
    ){
        List<User> users = userRepository.pageUser(roleId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id,
                                        @Valid @RequestBody UserUpdateDTO user,
                                        BindingResult result
                                        ){
        try {
            if (result.hasErrors()){
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }

            return ResponseEntity.ok(userService.updateUser(id, user));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("doiMatKhau/{id}")
    public ResponseEntity<?> doiMatKhauNguoiDung(
            @PathVariable Integer id,
            @Valid
            @RequestBody DoiMatKhauRequest request, BindingResult result) {

        if (result.hasErrors()) {
            List<String> listErorrs = result.getFieldErrors().stream().
                    map(errors -> errors.getDefaultMessage()).toList();
            return ResponseEntity.badRequest().body(listErorrs);
        }
        boolean result1 = userService.AdminDoiMK(id, request.getMatKhauMoi());
        if (result1) {
            return ResponseEntity.ok("Đã đổi mật khẩu thành công cho người dùng ID: " + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng.");
        }
    }

    @GetMapping("/auth/social-login")
    public ResponseEntity<?> social(@RequestParam("login-type") String loginType,
                                    HttpServletRequest request
                                    ){
        loginType = loginType.trim().toLowerCase();
        String url = authService.generateAuthUrl(loginType);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/auth/social/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("login-type") String loginType,
            HttpServletRequest request
    ){
        try {
        Map<String, Object> userInfo = authService.authenticateAndFetchProfile(code, loginType);
        if (userInfo == null){
            return ResponseEntity.badRequest().body("Fail to authenticate");
        }
        String accountId = "";
        String name = "";
        String email ="";

            if (loginType.trim().equals("google")){
                accountId = (String) Objects.requireNonNullElse(userInfo.get("sub"), "");
                name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
                email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
            }else if(loginType.trim().equals("facebook")){
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
                return ResponseEntity.badRequest().body("Tài khoản của bạn đã bị BAN");
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
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi xử lý đăng nhập " + loginType + ": " + e.getMessage());
        }
    }
}