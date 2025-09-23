package com.example.demo.Controller;

import com.example.demo.DTOs.SPYeuThichDTO;
import com.example.demo.DTOs.ViGiamGiaDTO;
import com.example.demo.DTOs.WishListDTO;
import com.example.demo.Entity.SanPhamYeuThich;
import com.example.demo.Entity.ViPhieuGiamGia;
import com.example.demo.Entity.WishList;
import com.example.demo.Repository.WishListRepository;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Responses.PhieuGiamGiaResponse;
import com.example.demo.Responses.SpYeuThichResponse;
import com.example.demo.Service.SPYeuThichService;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lego-store/san-pham-yeu-thich")
@RequiredArgsConstructor
public class SpYeuThichController {
    private final SPYeuThichService spYeuThichService;
    private final WishListRepository wishListRepository;

    @PostMapping("/create-wishlist")
    public ResponseEntity<?> createWL(@Valid @RequestBody WishListDTO dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            WishList wl = spYeuThichService.createWL(dto);
            return ResponseEntity.ok(wl);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @GetMapping("/get-wishlist")
    public ResponseEntity<?> getWLs(){
        return ResponseEntity.ok(wishListRepository.findAll());
    }

    @PutMapping("/update-wishlist/{idWL}")
    public ResponseEntity<?> updateWL(@PathVariable Integer idWL, @RequestBody WishListDTO wishListDTO) {
        try {
            if (wishListDTO.getTen() == null || wishListDTO.getTen().isBlank()){
                throw new RuntimeException("Khong de trong ten");
            }
            WishList wl = spYeuThichService.updateWL(idWL, wishListDTO.getTen());
            return ResponseEntity.ok(wl);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @DeleteMapping("/delete-wishlist/{idWL}")
    public ResponseEntity<?> deleteWL(@PathVariable Integer idWL){
        try {
            spYeuThichService.deleteWL(idWL);
            return ResponseEntity.ok(new ErrorResponse(200, "Xoa wishlist thanh cong"));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }


    @PostMapping("/them-yeu_thich")
    public ResponseEntity<?> themYeuThich(@Valid @RequestBody SPYeuThichDTO dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                String message = String.join(", ", result.getFieldErrors().stream().map(errors -> errors.getDefaultMessage()).toList());
                return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
            }
            SanPhamYeuThich sp = spYeuThichService.addSPyeuThich(dto);
            return ResponseEntity.ok(new ErrorResponse(200, "thêm yêu thích thành công"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @GetMapping("/user/{wishlistId}")
    public ResponseEntity<?> getSpTrongWishList(
            @PathVariable Integer wishlistId){
        try {
            wishListRepository.findById(wishlistId).orElseThrow(() -> new RuntimeException("Không tìm thấy id wishlist"));
            List<SpYeuThichResponse> list = spYeuThichService.getSanPhamYeuThich(wishlistId);
            return ResponseEntity.ok(list);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> xoaYeuThich(
            @RequestParam("wish_list_id") Integer wlId
            ,@RequestParam("sp_id") Integer spId){
        try {
            spYeuThichService.deleteSp(spId, wlId);
            return ResponseEntity.ok(new ErrorResponse(200,"Bỏ yêu thích thành công"));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ErrorResponse(500, e.getMessage()));
        }
    }
}
