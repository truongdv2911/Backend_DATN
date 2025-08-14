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

import java.text.DecimalFormat;

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

    public void sendDuyetPhieuHoanEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true để cho phép HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }

    private String buildHtmlContent(HoaDonEmailDTO req) {
        StringBuilder productsHtml = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#,###");

        // Format sản phẩm
        for (SanPhamHoaDonEmail product : req.getListSp()) {
            productsHtml.append(String.format("""
            <tr>
                <td>%s</td>
                <td>%s</td>
                <td>%sđ</td>
                <td>%d</td>
                <td>%sđ</td>
            </tr>
        """,
                    product.getMa(),
                    product.getTen(),
                    df.format(toNumber(product.getGia())),
                    product.getSoLuong(),
                    df.format(toNumber(product.getTongTien()))
            ));
        }

        // Chỉ in nếu có tiền giảm
        String tienGiamHtml = "";
        if (req.getTienGiam() != null) {
            tienGiamHtml = "<h3>Số tiền được giảm : <span style=\"color:green;\">" +
                    df.format(toNumber(req.getTienGiam())) + "đ</span></h3>";
        }

        // Chỉ in nếu có phí ship
        String phiShipHtml = "";
        if (req.getPhiShip() != null) {
            phiShipHtml = "<h3>Phí ship: <span style=\"color:green;\">" +
                    df.format(toNumber(req.getPhiShip())) + "đ</span></h3>";
        }

        return """
        <div style="font-family:sans-serif; font-size:14px; color:#333;">
            <div style="background: linear-gradient(90deg, #ff4b2b, #ff416c);
                        color: #fff;
                        padding: 15px 20px;
                        border-radius: 5px 5px 0 0;
                        text-align: center;
                        font-size: 20px;
                        font-weight: bold;
                        letter-spacing: 1px;">
                ✅ ĐẶT HÀNG THÀNH CÔNG
            </div>

            <div style="padding: 15px; background-color: #fafafa; border: 1px solid #ddd; border-top: none; border-radius: 0 0 5px 5px;">
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Cảm ơn Anh/Chị đã đặt hàng tại <strong>Lego My Kingdom</strong>!</p>
                <p>Đơn hàng của Anh/Chị đã được tiếp nhận, chúng tôi sẽ nhanh chóng liên hệ.</p>

                <h3>Thông tin đơn hàng</h3>
                <p><strong>Mã đơn hàng:</strong> %s</p>
                <p><strong>Ngày đặt hàng:</strong> %s</p>

                <h3>Sản phẩm</h3>
                <table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse; width:100%%; text-align:center;'>
                    <thead style="background-color:#f2f2f2;">
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

                %s
                %s
                <h3>Tổng cộng: <span style="color:green;">%sđ</span></h3>

                <p style="margin-top:30px;">Tra cứu thông tin hóa đơn, Tra cứu hành trình đơn hàng vui lòng truy cập 
                    <a href="http://localhost:3000/hoaDon/%d" style="color:#ff4b2b;">tại đây</a>
                </p>
                <p style="margin-top:30px;">Trân trọng,<br/>Cửa hàng Lego My Kingdom !</p>
            </div>
        </div>
    """.formatted(
                req.getTenKH(),
                req.getMaHD(),
                req.getNgayTao(),
                productsHtml.toString(),
                req.getPttt(),
                req.getPtvc(),
                req.getDiaChi(),
                tienGiamHtml,
                phiShipHtml,
                df.format(toNumber(req.getTotalAmount())),
                req.getIdHD()
        );
    }

    private Number toNumber(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return (Number) value;
        try {
            return new java.math.BigDecimal(value.toString().replaceAll("[^0-9.-]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
} 