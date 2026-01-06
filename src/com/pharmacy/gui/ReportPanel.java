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

public class ReportPanel extends JPanel {
    private JTable tblHoaDon;
    private DefaultTableModel modelHoaDon;
    private JLabel lblRevenue, lblProfit, lblOrders;
    private final Color COLOR_PRIMARY = new Color(0, 102, 204);

    public ReportPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setBackground(new Color(245, 245, 245));

        // --- 1. Dashboard (Tổng quan) ---
        JPanel pnlTop = new JPanel(new GridLayout(1, 3, 30, 0));
        pnlTop.setOpaque(false);
        pnlTop.setPreferredSize(new Dimension(0, 120));

        lblRevenue = new JLabel("0 đ");
        lblProfit = new JLabel("0 đ");
        lblOrders = new JLabel("0");

        pnlTop.add(createCard("TỔNG DOANH THU", lblRevenue, COLOR_PRIMARY, "💰"));
        pnlTop.add(createCard("LỢI NHUẬN ", lblProfit, new Color(40, 167, 69),"💵"));
        pnlTop.add(createCard("TỔNG ĐƠN HÀNG", lblOrders, new Color(255, 153, 0),"🧾"));
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. Table Section (Hiển thị chi tiết từng sản phẩm) ---
        String[] headers = {"Mã HĐ", "Ngày Lập", "Sản phẩm lẻ", "Số lượng", "Mã KH", "Thành Tiền", "Lợi Nhuận"};
        modelHoaDon = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblHoaDon = new JTable(modelHoaDon);
        tblHoaDon.setRowHeight(40); // Giảm chiều cao dòng
        // Định dạng căn lề
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblHoaDon.getColumnCount(); i++) {
            tblHoaDon.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JPanel pnlTable = new JPanel(new BorderLayout(10, 10));
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(new TitledBorder(null, "Chi tiết giao dịch từng mặt hàng", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), COLOR_PRIMARY));

        JScrollPane scroll = new JScrollPane(tblHoaDon);
        scroll.getViewport().setBackground(Color.WHITE);
        pnlTable.add(scroll, BorderLayout.CENTER);

        add(pnlTable, BorderLayout.CENTER);
        loadData();
    }

    private JPanel createCard(String title, JLabel lblValue, Color c, String iconEmoji) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, c), // Đường kẻ màu bên trái
                new EmptyBorder(15, 20, 15, 20)
        ));

        // 1. Tiêu đề nhỏ
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 12));
        t.setForeground(Color.GRAY);

        // 2. Phần nội dung chứa Số tiền và Icon
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        // -- Số tiền (Bên trái)
        lblValue.setFont(new Font("Arial", Font.BOLD, 24));
        lblValue.setForeground(new Color(50, 50, 50));
        pnlContent.add(lblValue, BorderLayout.WEST);

        // -- Icon Emoji (Bên phải)
        JLabel lblIcon = new JLabel(iconEmoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        pnlContent.add(lblIcon, BorderLayout.EAST);

        // 3. Đưa tất cả vào Card
        card.add(t, BorderLayout.NORTH);
        card.add(pnlContent, BorderLayout.CENTER);

        return card;
    }

    public void loadData() {
        modelHoaDon.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");
        double totalRev = 0;
        double totalPrf = 0;
        int totalOrdersCount = 0;
        int lastMaHD = -1; // Biến phụ để đếm đúng số lượng đơn hàng

        // Lấy trực tiếp từng dòng sản phẩm từ bảng chi tiết
        String sql = "SELECT h.MaHD, h.NgayInHoaDon, s.TenSP, ct.SoLuong, ct.ThanhTien, " +
                " (ct.ThanhTien - (s.GiaNhap * ct.SoLuong)) as LoiNhuanDong " +
                " FROM HoaDon h " +
                " JOIN ChiTietHoaDon ct ON h.MaHD = ct.MaHD " +
                " JOIN SanPham s ON ct.MaSP = s.MaSP " +
                " ORDER BY h.MaHD DESC";

        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int maHD = rs.getInt("MaHD");
                double revDong = rs.getDouble("ThanhTien");
                double prfDong = rs.getDouble("LoiNhuanDong");

                modelHoaDon.addRow(new Object[]{
                        maHD,
                        rs.getDate("NgayInHoaDon"),
                        rs.getString("TenSP"),
                        rs.getInt("SoLuong"),
                        "KH" + (maHD + 100),
                        df.format(revDong) + " đ",
                        df.format(prfDong) + " đ"
                });

                totalRev += revDong;
                totalPrf += prfDong;

                // Logic đếm đơn hàng: Chỉ tăng khi mã hóa đơn thay đổi (vì 1 HĐ giờ có nhiều dòng)
                if (maHD != lastMaHD) {
                    totalOrdersCount++;
                    lastMaHD = maHD;
                }
            }

            lblRevenue.setText(df.format(totalRev) + " đ");
            lblProfit.setText(df.format(totalPrf) + " đ");
            lblOrders.setText(String.valueOf(totalOrdersCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}