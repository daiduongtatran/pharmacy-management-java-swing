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
        Connection conn = null;
        try {
            conn = Database.getConnection(); // Mở kết nối
            String sql = "SELECT MaSP, TenSP, MaKH, TongTien FROM dbo.SanPham ORDER BY MaSP DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
            // Lưu ý: Kết nối này sẽ vẫn mở để MainFrame đọc dữ liệu.
            // Sau khi MainFrame đọc xong, nó sẽ tự đóng hoặc chờ Garbage Collector.
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}