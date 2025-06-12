
package com.example.demo.Controller;

import com.example.demo.DTOs.DTOlogin;
import com.example.demo.DTOs.DTOuser;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.ListUserResponse;
import com.example.demo.Responses.LoginResponse;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/lego-store/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final GioHangService gioHangService;
    private final AuthenticationManager authenticationManager;


    @GetMapping("/success")
    public ResponseEntity<?> handleGoogleLogin(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            // Kiểm tra hoặc tạo người dùng trong database
            DTOuser dtoUser = new DTOuser();
            dtoUser.setEmail(email);
            dtoUser.setTen(name);
            dtoUser.setMatKhau("123456@");
            // Bạn có thể thêm các thuộc tính khác nếu cần

            try {
                Optional<User> user = userRepository.findByEmail(email);
                if (user.isEmpty()) {
                    // Tạo người dùng mới nếu chưa tồn tại
                    userService.createUser(dtoUser);
                }
                // Lưu thông tin vào session
                HttpSession session = request.getSession(true);
                session.setAttribute("username", email);
                return ResponseEntity.ok(new LoginResponse(user.get().getId(), user.get().getTen(), user.get().getEmail(), "Đăng nhập bằng Google thành công"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new LoginResponse(null, null,null, "Lỗi khi xử lý đăng nhập Google"));
            }
        }
        return ResponseEntity.badRequest().body(new LoginResponse(null, null,null, "Xác thực không hợp lệ"));
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

            User user = userRepository.findByEmail(dtOlogin.getEmail()).orElseThrow(()-> new RuntimeException("khong tim thay email user"));
            gioHangService.getOrCreateCart(user.getId());

            return ResponseEntity.ok(new LoginResponse(user.getId(), user.getTen(), dtOlogin.getEmail(), "Dang nhap thanh cong"));
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
            return ResponseEntity.ok(userService.createUser(user));
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
            @RequestParam("page") int pageNo,
            @RequestParam("limit") int limit
    ){
        PageRequest pageRequest = PageRequest.of(pageNo, limit, Sort.by("ngayTao").descending());
        Page<User> users = userService.pageUser(pageRequest);
        int totalPage = users.getTotalPages();
        List<User> listUser = users.getContent();
        return ResponseEntity.ok(new ListUserResponse(listUser, totalPage));
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id,
                                        @Valid @RequestBody DTOuser user,
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

}