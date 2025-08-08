package com.example.demo.Entity;

public class TrangThaiHoaDon {
    public static final String PENDING = "Đang xử lý";// Khi khách hàng đặt hàng xong
    public static final String PROCESSING = "Đã xác nhận";//Nhân viên đã xác nhận đơn hàng
    public static final String PACKING = "Đang đóng gói"; // Đơn hàng đang được chuẩn bị
    public static final String SHIPPED = "Đang vận chuyển";// Đơn hàng đã rời kho và đang trên đường giao
    public static final String DELIVERED = "Đã giao";// Đơn hàng đã giao tới khách
    public static final String COMPLETED = "Hoàn tất";// Khách đã nhận hàng và không có khiếu nại
    public static final String CANCELLED = "Đã hủy";// Đơn hàng đã bị hủy (do khách hoặc hệ thống)
    public static final String FAILED = "Thất bại";// Xử lý đơn hàng bị lỗi (ví dụ: thanh toán không thành công)

    public static final String RETURN = "Hoàn hàng";     // Khách gửi yêu cầu hoàn
}
