package com.pharmacy.service;

import com.pharmacy.connectdb.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ThongKeService {

    // 1. Lấy tổng doanh thu (Hiển thị Dashboard MainFrame)
    public double getTongDoanhThu() {
        double total = 0;
        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(TongTien) FROM HoaDon")) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    // 2. Lấy số lượng đơn hàng (Hiển thị Dashboard MainFrame)
    public int getSoDonHang() {
        int count = 0;
        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM HoaDon")) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }
    // 3. Lấy danh sách thuốc (Cho Dashboard hiện tại)
    public ResultSet getDanhSachThuoc() {
        try {
            Connection con = Database.getConnection();
            String sql = "SELECT * FROM SanPham";
            PreparedStatement pst = con.prepareStatement(sql);
            return pst.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}