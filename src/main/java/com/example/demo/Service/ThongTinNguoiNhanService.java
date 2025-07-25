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
            if (thongTinNguoiNhan.size() == 0){
                return  thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(null,
                        dtOthongTinNguoiNhan.getHoTen(),
                        dtOthongTinNguoiNhan.getSdt(),
                        dtOthongTinNguoiNhan.getDuong(),
                        dtOthongTinNguoiNhan.getXa(),
                        dtOthongTinNguoiNhan.getThanhPho(),
                        1,
                        userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                ));
            }else {
                if (dtOthongTinNguoiNhan.getIsMacDinh() == 1){
                    ThongTinNguoiNhan nguoiNhan= thongTinNguoiNhanRepository.findByIsMacDinh(1);
                    nguoiNhan.setIsMacDinh(0);

                    thongTinNguoiNhanRepository.save(nguoiNhan);
                    return  thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(null,
                            dtOthongTinNguoiNhan.getHoTen(),
                            dtOthongTinNguoiNhan.getSdt(),
                            dtOthongTinNguoiNhan.getDuong(),
                            dtOthongTinNguoiNhan.getXa(),
                            dtOthongTinNguoiNhan.getThanhPho(),
                            1,
                            userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                    ));
                }else {
                    return  thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(null,
                            dtOthongTinNguoiNhan.getHoTen(),
                            dtOthongTinNguoiNhan.getSdt(),
                            dtOthongTinNguoiNhan.getDuong(),
                            dtOthongTinNguoiNhan.getXa(),
                            dtOthongTinNguoiNhan.getThanhPho(),
                            0,
                            userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                    ));
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public ThongTinNguoiNhan updateThongTin(Integer id, DTOthongTinNguoiNhan dtOthongTinNguoiNhan) throws Exception {
        try {
            List<ThongTinNguoiNhan> thongTinNguoiNhan = thongTinNguoiNhanRepository.findAll();
            if (thongTinNguoiNhan.size() == 1) {
                return thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(id,
                        dtOthongTinNguoiNhan.getHoTen(),
                        dtOthongTinNguoiNhan.getSdt(),
                        dtOthongTinNguoiNhan.getDuong(),
                        dtOthongTinNguoiNhan.getXa(),
                        dtOthongTinNguoiNhan.getThanhPho(),
                        1,
                        userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                ));
            } else if (thongTinNguoiNhan.size() > 1) {
                if (dtOthongTinNguoiNhan.getIsMacDinh() == 1) {
                    ThongTinNguoiNhan nguoiNhan = thongTinNguoiNhanRepository.findByIsMacDinh(1);
                    nguoiNhan.setIsMacDinh(0);
                    thongTinNguoiNhanRepository.save(nguoiNhan);
                    return thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(id,
                            dtOthongTinNguoiNhan.getHoTen(),
                            dtOthongTinNguoiNhan.getSdt(),
                            dtOthongTinNguoiNhan.getDuong(),
                            dtOthongTinNguoiNhan.getXa(),
                            dtOthongTinNguoiNhan.getThanhPho(),
                            1,
                            userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                    ));
                } else {
                    ThongTinNguoiNhan nguoiNhan = thongTinNguoiNhanRepository.findById(id).orElseThrow(()-> new RuntimeException("Khong tim thay id user"));
                    if (nguoiNhan.getIsMacDinh()==1){
                        return thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(id,
                                dtOthongTinNguoiNhan.getHoTen(),
                                dtOthongTinNguoiNhan.getSdt(),
                                dtOthongTinNguoiNhan.getDuong(),
                                dtOthongTinNguoiNhan.getXa(),
                                dtOthongTinNguoiNhan.getThanhPho(),
                                1,
                                userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                        ));
                    }else {
                        return thongTinNguoiNhanRepository.save(new ThongTinNguoiNhan(id,
                                dtOthongTinNguoiNhan.getHoTen(),
                                dtOthongTinNguoiNhan.getSdt(),
                                dtOthongTinNguoiNhan.getDuong(),
                                dtOthongTinNguoiNhan.getXa(),
                                dtOthongTinNguoiNhan.getThanhPho(),
                                0,
                                userRepository.findById(dtOthongTinNguoiNhan.getIdUser()).orElseThrow(() -> new RuntimeException("Khong tim thay id user"))
                        ));
                    }

                }
            }else {
                throw new Exception("Lỗi khi cập nhật thông tin");
            }
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
