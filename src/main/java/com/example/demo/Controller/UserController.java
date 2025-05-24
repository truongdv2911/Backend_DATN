<<<<<<< HEAD
    package com.example.demo.Controller;

    import com.example.demo.DTOs.DTOlogin;
    import com.example.demo.DTOs.DTOuser;
    import com.example.demo.Responses.LoginResponse;
    import com.example.demo.Service.UserService;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpSession;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.modelmapper.ModelMapper;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Controller;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Map;

    @Controller
    @RequiredArgsConstructor
    @RestController
    @RequestMapping("api/lego-store/user")
    public class UserController {
        private final UserService userService;
        private final AuthenticationManager authenticationManager;

        @PostMapping("/login")
        public ResponseEntity<?> login(@Valid @RequestBody DTOlogin dtOlogin, BindingResult result, HttpServletRequest request) {
            try {
                if (result.hasErrors()) {
                    List<String> listErorrs = result.getFieldErrors().stream().
                            map(errors -> errors.getDefaultMessage()).toList();
                    return ResponseEntity.badRequest().body(listErorrs);
                }
    //            tạm comment token nhé
    //            String token = userService.login(dtOlogin);
    //            return ResponseEntity.ok(new LoginResponse(token,"Dang nhap thanh cong"));

                //Lưu vào session or localStogare
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dtOlogin.getEmail(), dtOlogin.getMatKhau()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                HttpSession hs= request.getSession(true);
                hs.setAttribute("username", dtOlogin.getEmail());
                return ResponseEntity.ok(new LoginResponse(dtOlogin.getEmail(), "Dang nhap thanh cong"));
            }    catch (Exception e){
                return ResponseEntity.badRequest().body(new LoginResponse(null,"Sai thong tin dang nhap"));
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


    }
=======
//package com.example.demo.Controller;
//
//import com.example.demo.DTOs.DTOlogin;
//import com.example.demo.DTOs.DTOuser;
//import com.example.demo.Responses.LoginResponse;
//import com.example.demo.Service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("api/lego-store/user")
//public class UserController {
//    private final UserService userService;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody DTOlogin dtOlogin, BindingResult result){
//        try {
//            if (result.hasErrors()){
//                List<String> listErorrs = result.getFieldErrors().stream().
//                        map(errors -> errors.getDefaultMessage()).toList();
//                return ResponseEntity.badRequest().body(listErorrs);
//            }
//            String token = userService.login(dtOlogin);
//            return ResponseEntity.ok(new LoginResponse(token,"Dang nhap thanh cong"));
//        }catch (Exception e){
//            return ResponseEntity.badRequest().body(new LoginResponse(null,"Sai thong tin dang nhap"));
//        }
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<?> createUser(@Valid @RequestBody DTOuser user, BindingResult result){
//        try {
//            if (result.hasErrors()){
//                List<String> listErorrs = result.getFieldErrors().stream().
//                        map(errors -> errors.getDefaultMessage()).toList();
//                return ResponseEntity.badRequest().body(listErorrs);
//            }
//            return ResponseEntity.ok(userService.createUser(user));
//        }catch (Exception e){
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//}
>>>>>>> be_ky
