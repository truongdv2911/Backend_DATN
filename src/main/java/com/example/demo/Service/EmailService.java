package com.example.demo.Service;

import com.example.demo.DTOs.HoaDonEmailDTO;
import com.example.demo.DTOs.SanPhamHoaDonEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Yêu cầu đặt lại mật khẩu của bạn");
        message.setText("Mã OTP của bạn là: " + otp + "\n" + "Mã này sẽ hết hạn trong 3 phút.");
        mailSender.send(message);
    }

    public void sendOrderEmail(HoaDonEmailDTO request) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(request.getToEmail());
        helper.setSubject("Xác nhận đơn hàng #" + request.getMaHD());
        helper.setText(buildHtmlContent(request), true); // true = HTML

        mailSender.send(mimeMessage);
    }

    private String buildHtmlContent(HoaDonEmailDTO req) {
        StringBuilder productsHtml = new StringBuilder();

        for (SanPhamHoaDonEmail product : req.getListSp()) {
            productsHtml.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s ₫</td>
                    <td>%d</td>
                    <td>%s ₫</td>
                </tr>
            """, product.getMa(), product.getTen(), product.getGia(), product.getSoLuong(), product.getTongTien()));
        }

        return """
            <div style="font-family:sans-serif; font-size:14px; color:#333;">
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Cảm ơn Anh/Chị đã đặt hàng tại <strong>Lego My Kingdom</strong>!</p>
                <p>Đơn hàng của Anh/Chị đã được tiếp nhận, chúng tôi sẽ nhanh chóng liên hệ.</p>

                <h3>Thông tin đơn hàng</h3>
                <p><strong>Mã đơn hàng:</strong> %s</p>
                <p><strong>Ngày đặt hàng:</strong> %s</p>

                <h3>Sản phẩm</h3>
                <table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>
                    <thead>
                        <tr>
                            <th>Mã sản phẩm</th>
                            <th>Tên sản phẩm</th>
                            <th>Giá</th>
                            <th>Số lượng</th>
                            <th>Thành tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>

                <p><strong>Phương thức thanh toán:</strong> %s</p>
                <p><strong>Phương thức vận chuyển:</strong> %s</p>
                <p><strong>Địa chỉ giao hàng:</strong> %s</p>

                <h3>Tổng cộng: <span style="color:green;">%s ₫</span></h3>
                <p style="margin-top:30px;">Tra cứu thông tin hóa đơn, Tra cứu hành trình đơn hàng vui lòng truy cập hotro.sieutoc.com/%d</p>
                <p style="margin-top:30px;">Trân trọng,<br/>Cửa hàng Lego My Kingdom !</p>
            </div>
        """.formatted(
                req.getTenKH(),
                req.getMaHD(),
                req.getNgayTao(),
                productsHtml.toString(),
                req.getPttt(),
                req.getPtvc(),
                req.getDiaChi(),
                req.getTotalAmount(),
                req.getIdHD()
        );
    }
} 