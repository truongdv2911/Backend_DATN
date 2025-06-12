package com.example.demo.Service;

import com.example.demo.DTOs.DTOthongTinNguoiNhan;
import com.example.demo.Entity.ThongTinNguoiNhan;
import com.example.demo.Repository.ThongTinNguoiNhanRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ThongTinNguoiNhanService {
    private final ThongTinNguoiNhanRepository thongTinNguoiNhanRepository;
    private final UserRepository userRepository;

    public ThongTinNguoiNhan createThongTin(DTOthongTinNguoiNhan dtOthongTinNguoiNhan) throws Exception {
        try {
            List<ThongTinNguoiNhan> thongTinNguoiNhan = thongTinNguoiNhanRepository.findAll();
            return thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(null,
                    dtOthongTinNguoiNhan.getHoTen(),
                    dtOthongTinNguoiNhan.getSdt(),
                    dtOthongTinNguoiNhan.getDuong(),
                    dtOthongTinNguoiNhan.getXa(),
                    dtOthongTinNguoiNhan.getHuyen(),
                    dtOthongTinNguoiNhan.getThanhPho(),
                    thongTinNguoiNhan.size()== 0 ? 1 : 0,
                    userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                    ));
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public ThongTinNguoiNhan updateThongTin(Integer id,
            DTOthongTinNguoiNhan dtOthongTinNguoiNhan
    ) throws Exception
    {
        try {
            ThongTinNguoiNhan existing = thongTinNguoiNhanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi"));

                    existing.setHoTen(dtOthongTinNguoiNhan.getHoTen());
                    existing.setSdt(dtOthongTinNguoiNhan.getSdt());
                    existing.setDuong(dtOthongTinNguoiNhan.getDuong());
                    existing.setXa(dtOthongTinNguoiNhan.getXa());
                    existing.setHuyen(dtOthongTinNguoiNhan.getHuyen());
                    existing.setThanhPho(dtOthongTinNguoiNhan.getThanhPho());
                    existing.setIsMacDinh(dtOthongTinNguoiNhan.getIsMacDinh());
                    existing.setUser(userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() ->
                            new RuntimeException("Khong tim thay id user")));

             return thongTinNguoiNhanRepository.save(existing);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public String deleteThongTin(Integer id) throws Exception {
        if (thongTinNguoiNhanRepository.findById(id).isEmpty()){
            throw new Exception("Khong Tim thay id thong tin");
        }
        thongTinNguoiNhanRepository.deleteById(id);
        return "Xóa thông tin người nhận thành công";
    }


}
