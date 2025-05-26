
package com.example.demo.Controller;

import com.example.demo.DTOs.DTOlogin;
import com.example.demo.DTOs.DTOuser;
import com.example.demo.Responses.LoginResponse;
import com.example.demo.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RestController
@RequestMapping("api/lego-store/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody DTOlogin dtOlogin, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            String token = userService.login(dtOlogin);
            return ResponseEntity.ok(new LoginResponse(token, "Dang nhap thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, "Sai thong tin dang nhap"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody DTOuser user, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> listErorrs = result.getFieldErrors().stream().
                        map(errors -> errors.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(listErorrs);
            }
            return ResponseEntity.ok(userService.createUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
//login basic
//    @PostMapping("/loginBasic")
//    public ResponseEntity<?> login1(@Valid @RequestBody DTOlogin dtOlogin, BindingResult result){
//        try{


//            String mess = userService.login1(dtOlogin);
//            return ResponseEntity.ok(mess);
//        }catch (Exception e){
//            return ResponseEntity.badRequest().body(new LoginResponse(null,"Sai thong tin dang nhap"));
//        }
//    }

