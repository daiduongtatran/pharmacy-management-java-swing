package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductFrame extends JPanel {
    private JTable tbl;
    private DefaultTableModel model;
    private JTextArea txtNote;

    public ProductFrame() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // --- BÊN TRÁI: BẢNG SẢN PHẨM ---
        String[] headers = {"Mã SP", "Tên SP", "Loại", "Giá Nhập", "Giá Bán", "Xuất", "Tồn"};
        model = new DefaultTableModel(headers, 0);
        tbl = new JTable(model);
        tbl.setRowHeight(30);

        loadData(); // Tải dữ liệu từ SQL

        // --- BÊN PHẢI: CHI TIẾT ---
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setPreferredSize(new Dimension(250, 0));
        pnlRight.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));

        txtNote = new JTextArea("\n  Chọn sản phẩm để xem...");
        txtNote.setEditable(false);
        txtNote.setLineWrap(true);
        txtNote.setFont(new Font("Arial", Font.ITALIC, 13));
        pnlRight.add(new JScrollPane(txtNote), BorderLayout.CENTER);

        // Sự kiện click bảng
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbl.getSelectedRow() != -1) {
                String ten = model.getValueAt(tbl.getSelectedRow(), 1).toString();
                txtNote.setText("Sản phẩm: " + ten + "\n\nChức năng: Đặc trị...\nLưu ý: Để xa tầm tay trẻ em.");
            }
        });

        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlRight, BorderLayout.EAST);
    }

    private void loadData() {
        try {
            Connection con = Database.getConnection();
            if (con == null) {
                System.out.println("Lỗi: Connection null trong ProductFrame!");
                return;
            }

            // Xóa sạch dữ liệu cũ trên bảng giao diện trước khi nạp mới
            model.setRowCount(0);

            String sql = "SELECT * FROM SanPham";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next()){
                // Lấy dữ liệu theo đúng tên cột trong SQL Server
                Object[] row = {
                        rs.getInt("MaSP"),        // Cột 1: Mã (kiểu int identity)
                        rs.getString("TenSP"),     // Cột 2
                        rs.getString("LoaiSP"),    // Cột 3
                        rs.getDouble("GiaNhap"),   // Cột 4
                        rs.getDouble("GiaBan"),    // Cột 5
                        rs.getInt("HangXuat"),     // Cột 6
                        rs.getInt("TonKho")        // Cột 7
                };
                model.addRow(row);
            }
            System.out.println("Đã nạp dữ liệu từ SQL thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi nạp dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}