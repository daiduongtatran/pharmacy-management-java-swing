package com.pharmacy.service;

import com.pharmacy.connectdb.Database;
import java.sql.*;

public class ThongKeService {

    // Hàm 1: Lấy tổng doanh thu
    public double getTongDoanhThu() {
        double total = 0;
        String sql = "SELECT SUM(TongTien) FROM dbo.HoaDon";

        // Dùng try-with-resources để tự động đóng kết nối SAU KHI DÙNG XONG trong phạm vi hàm này
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Hàm 2: Lấy số lượng đơn hàng
    public int getSoDonHang() {
        int count = 0;
        String sql = "SELECT COUNT(MaHD) FROM dbo.HoaDon";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return count;

    }

    // Hàm 3: Lấy danh sách hóa đơn (Để đổ vào bảng)
    // Lưu ý: Hàm trả về ResultSet cần cẩn thận.
    // Nếu đóng Connection ngay ở đây thì ResultSet sẽ chết theo.
    // -> Giải pháp: Trả về CachedRowSet hoặc Vector, NHƯNG để nhanh nhất cho bạn lúc này:
    // Ta sẽ KHÔNG dùng try-with-resources cho Connection ở hàm này, mà để MainFrame quản lý,
    // HOẶC dùng mô hình lấy dữ liệu ra Object/Vector.

    public ResultSet getDanhSachThuoc() {
        try {
            //Lấy connection trực tiếp từ lớp Database của bạn
            Connection conn = Database.getConnection();
            String sql = "SELECT MaSP, TenSP, LoaiSP, DonVi, TonKho, HanDung FROM SanPham";
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách thuốc: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}