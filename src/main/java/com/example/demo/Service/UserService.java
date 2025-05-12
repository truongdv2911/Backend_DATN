package com.example.demo.Service;

import com.example.demo.DTOs.DTOlogin;
import com.example.demo.DTOs.DTOuser;
import com.example.demo.Entity.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public User createUser(DTOuser dtoUser){
        if (userRepository.existsByEmail(dtoUser.getEmail())){
            throw new DataIntegrityViolationException("Email da ton tai");
        }
        User user = new User(null, dtoUser.getTen(), dtoUser.getEmail(), dtoUser.getMatKhau(),dtoUser.getSdt()
                ,dtoUser.getNgaySinh(), dtoUser.getDiaChi(), 1, dtoUser.getFacebookId(),
                dtoUser.getGoogleId(), roleRepository.findById(2).orElseThrow(()-> new RuntimeException("Khong tim thay role")));
        return userRepository.save(user);
    }

    public String login(DTOlogin dtOlogin) throws Exception {
        Optional<User> user = userRepository.findByEmail(dtOlogin.getEmail());
        if(user.isPresent() && user.get().getMatKhau().equals(dtOlogin.getMatKhau())){
            return "Dang nhap thanh cong";
        }else{
            throw new Exception("Sai thong tin dang nhap");
        }
    }
}
