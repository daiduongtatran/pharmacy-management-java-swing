package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Random;

public class ReportPanel extends JPanel {
    private JTable tblHoaDon;
    private DefaultTableModel modelHoaDon;
    private JLabel lblRevenue, lblProfit, lblOrders;
    private final Color COLOR_PRIMARY = new Color(0, 102, 204);

    public ReportPanel() {
        setLayout(new BorderLayout(20, 20)); // Tăng khoảng cách các thành phần chính
        setBorder(new EmptyBorder(25, 25, 25, 25)); // Padding lề ngoài lớn hơn
        setBackground(new Color(245, 245, 245));

        // --- 1. Top Stats Cards (Có khoảng cách đẹp hơn) ---
        JPanel pnlTop = new JPanel(new GridLayout(1, 3, 30, 0)); // 30 là khoảng cách giữa các ô
        pnlTop.setOpaque(false);
        pnlTop.setPreferredSize(new Dimension(0, 120)); // Tăng chiều cao các ô

        lblRevenue = new JLabel("0 đ");
        lblProfit = new JLabel("0 đ");
        lblOrders = new JLabel("0");

        pnlTop.add(createCard("TỔNG DOANH THU", lblRevenue, COLOR_PRIMARY));
        pnlTop.add(createCard("LỢI NHUẬN ", lblProfit, new Color(40, 167, 69)));
        pnlTop.add(createCard("TỔNG ĐƠN HÀNG", lblOrders, new Color(255, 153, 0)));
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. Table Section ---
        String[] headers = {"Mã HĐ", "Ngày Lập", "Sản phẩm", "Số lượng", "Mã KH", "Doanh Thu", "Lợi Nhuận"};
        modelHoaDon = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblHoaDon = new JTable(modelHoaDon);
        tblHoaDon.setRowHeight(60); // Tăng chiều cao dòng để đủ chỗ cho nhiều tên thuốc (xuống dòng)

        // Căn lề và định dạng
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblHoaDon.getColumnModel().getColumn(0).setCellRenderer(center);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);
        tblHoaDon.getColumnModel().getColumn(4).setCellRenderer(right);
        tblHoaDon.getColumnModel().getColumn(5).setCellRenderer(right);

        JPanel pnlTable = new JPanel(new BorderLayout(10, 10));
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(new TitledBorder(null, "Chi tiết giao dịch", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), COLOR_PRIMARY));

        JScrollPane scroll = new JScrollPane(tblHoaDon);
        scroll.getViewport().setBackground(Color.WHITE);
        pnlTable.add(scroll, BorderLayout.CENTER);
        JPanel pnlTool = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTool.setOpaque(false);
        pnlTable.add(pnlTool, BorderLayout.NORTH);

        add(pnlTable, BorderLayout.CENTER);
        loadData();
    }

    private JPanel createCard(String title, JLabel lblValue, Color c) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, c), // Đường kẻ bên trái
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 12));
        t.setForeground(Color.GRAY);

        lblValue.setFont(new Font("Arial", Font.BOLD, 24));
        lblValue.setForeground(new Color(50, 50, 50));

        card.add(t, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    public void loadData() {
        modelHoaDon.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");
        Random rd = new Random();
        double totalRev = 0;
        double totalPrf = 0;
        int totalOrders = 0;

        try (Connection con = Database.getConnection()) {
            // 1. SQL: Lấy thông tin hóa đơn và gộp tên thuốc
            String sql = "SELECT h.MaHD, h.NgayInHoaDon, h.TongTien, " +
                    "(SELECT s.TenSP + ', ' FROM ChiTietHoaDon ct JOIN SanPham s ON ct.MaSP = s.MaSP " +
                    " WHERE ct.MaHD = h.MaHD FOR XML PATH('')) as DSTen, " +
                    "(SELECT SUM(SoLuong) FROM ChiTietHoaDon WHERE MaHD = h.MaHD) as TongSL " +
                    "FROM HoaDon h ORDER BY h.MaHD DESC";

            ResultSet rs = con.createStatement().executeQuery(sql);

            // Cấu hình Căn lề giữa cho bảng
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            for (int i = 0; i < tblHoaDon.getColumnCount(); i++) {
                tblHoaDon.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            while (rs.next()) {
                double rev = rs.getDouble("TongTien");
                double prf = rev * 0.3;
                int tongSL = rs.getInt("TongSL");

                // 2. Xử lý xóa dấu phẩy thừa ở cuối danh sách thay vì dùng <br>
                String dsRaw = rs.getString("DSTen");
                if (dsRaw != null && dsRaw.endsWith(", ")) {
                    dsRaw = dsRaw.substring(0, dsRaw.length() - 2);
                }
                // Thay thế dấu phẩy bằng thẻ <br> để xuống dòng đẹp trong HTML
                String htmlTenThuoc = "<html><div style='text-align: center;'>" +
                        (dsRaw != null ? dsRaw.replace(", ", "<br>") : "") +
                        "</div></html>";

                String maKHNgauNhien = "KH" + (100 + rd.nextInt(900));

                modelHoaDon.addRow(new Object[]{
                        rs.getInt("MaHD"),
                        rs.getDate("NgayInHoaDon"),
                        htmlTenThuoc,
                        tongSL,
                        maKHNgauNhien,
                        df.format(rev) + " đ",
                        df.format(prf) + " đ"
                });

                totalRev += rev;
                totalPrf += prf;
                totalOrders++;
            }

            lblRevenue.setText(df.format(totalRev) + " đ");
            lblProfit.setText(df.format(totalPrf) + " đ");
            lblOrders.setText(String.valueOf(totalOrders));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}