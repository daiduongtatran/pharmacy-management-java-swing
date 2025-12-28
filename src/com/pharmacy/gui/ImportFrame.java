package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

public class ImportFrame extends JPanel {

    private JTable tblNhapHang;
    private DefaultTableModel model;
    private JLabel lblTongTien;
    private JButton btnThemDong, btnXoaDong, btnLuuPhieu;

    private final Color COLOR_PRIMARY = new Color(0, 150, 136); // Teal color cho Nhập hàng
    private final Color COLOR_DANGER = new Color(220, 53, 69);
    private final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);

    public ImportFrame() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        // --- Header ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ NHẬP KHO");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_PRIMARY);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- Bảng Nhập Liệu ---
        String[] columns = {"Mã SP", "Tên Sản Phẩm", "Số Lượng", "Giá Nhập", "Giá Bán Mới", "Thành Tiền"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Không cho sửa cột Thành Tiền (vì tự tính)
                return column != 5;
            }
        };


        // Ép bảng kết thúc chỉnh sửa khi mất focus

        tblNhapHang = new JTable(model);
        tblNhapHang.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tblNhapHang.setRowHeight(35);
        tblNhapHang.getTableHeader().setFont(FONT_BOLD);
        tblNhapHang.getTableHeader().setBackground(COLOR_PRIMARY);
        tblNhapHang.getTableHeader().setForeground(Color.WHITE);


        // Lắng nghe sự kiện sửa ô để tự tính Thành Tiền
        model.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            // Nếu sửa SL (cột 2) hoặc Giá Nhập (cột 3) thì tính lại Thành Tiền
            if (col == 2 || col == 3) {
                updateLineTotal(row);
            }
        });

        JScrollPane scroll = new JScrollPane(tblNhapHang);
        scroll.setBorder(BorderFactory.createTitledBorder(null, "Danh sách hàng nhập",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, COLOR_PRIMARY));
        add(scroll, BorderLayout.CENTER);

        // --- Panel Chức năng & Tổng tiền ---
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);

        // Nút bấm bên trái
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlButtons.setOpaque(false);

        btnThemDong = new JButton("➕ Thêm dòng mới");
        styleButton(btnThemDong, COLOR_PRIMARY);
        btnThemDong.addActionListener(e -> {
            // Sử dụng chuỗi rỗng "" thay vì số 0 cho các cột nhập liệu
            // Cột 0: Mã SP, Cột 1: Tên, Cột 2: SL, Cột 3: Giá Nhập, Cột 4: Giá Bán, Cột 5: Thành Tiền
            model.addRow(new Object[]{"", "", "", "", "", 0});
        });
        btnXoaDong = new JButton("❌ Xóa dòng chọn");
        styleButton(btnXoaDong, COLOR_DANGER);
        btnXoaDong.addActionListener(e -> {
            int row = tblNhapHang.getSelectedRow();
            if (row != -1) model.removeRow(row);
            tinhTongPhieu();
        });


        pnlButtons.add(btnThemDong);
        pnlButtons.add(btnXoaDong);

        // Tổng tiền và Lưu bên phải
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pnlAction.setOpaque(false);

        lblTongTien = new JLabel("Tổng cộng: 0 đ");
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 22));

        btnLuuPhieu = new JButton("✅ LƯU PHIẾU NHẬP");
        styleButton(btnLuuPhieu, COLOR_SUCCESS);
        btnLuuPhieu.setPreferredSize(new Dimension(200, 45));
        btnLuuPhieu.addActionListener(e -> luuDuLieuVaoDB());

        pnlAction.add(lblTongTien);
        pnlAction.add(btnLuuPhieu);

        pnlBottom.add(pnlButtons, BorderLayout.WEST);
        pnlBottom.add(pnlAction, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void updateLineTotal(int row) {
        try {
            // Lấy dữ liệu từ cột 2 (Số lượng) và cột 3 (Giá nhập)
            Object qtyObj = model.getValueAt(row, 2);
            Object priceObj = model.getValueAt(row, 3);

            if (qtyObj != null && priceObj != null) {
                int sl = Integer.parseInt(qtyObj.toString());
                double gia = Double.parseDouble(priceObj.toString());

                double thanhTien = sl * gia;

                // Ghi vào cột 5 (Thành tiền)
                model.setValueAt(thanhTien, row, 5);

                // Sau khi có thành tiền từng dòng, mới gọi tính tổng cả bảng
                tinhTongPhieu();
            }
        } catch (Exception e) {
            // Nếu nhập chữ thay vì số sẽ rơi vào đây
            model.setValueAt(0, row, 5);
        }
    }

    private void tinhTongPhieu() {
        double tong = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 5); // Phải là cột 5
            if (val != null && !val.toString().isEmpty()) {
                tong += Double.parseDouble(val.toString());
            }
        }
        lblTongTien.setText("Tổng cộng: " + new DecimalFormat("#,###").format(tong) + " đ");
    }

    private void luuDuLieuVaoDB() {
        if (model.getRowCount() == 0) return;

        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            // Lưu phiếu nhập tổng
            String sqlPN = "INSERT INTO PhieuNhap (NgayNhap, TongTien) VALUES (GETDATE(), ?)";
            PreparedStatement pstPN = con.prepareStatement(sqlPN, Statement.RETURN_GENERATED_KEYS);
            double tongTien = Double.parseDouble(lblTongTien.getText().replaceAll("[^0-9]", ""));
            pstPN.setDouble(1, tongTien);
            pstPN.executeUpdate();

            ResultSet rsKey = pstPN.getGeneratedKeys();
            int maPN = 0; if (rsKey.next()) maPN = rsKey.getInt(1);

            // SQL Cập nhật Sản Phẩm: Tăng tồn kho VÀ cập nhật Giá Bán mới
            String sqlUpdateSP = "UPDATE SanPham SET TonKho = TonKho + ?, GiaBan = ? WHERE MaSP = ?";
            String sqlCT = "INSERT INTO ChiTietPhieuNhap (MaPN, MaSP, SoLuong, GiaNhap, ThanhTien) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pstSP = con.prepareStatement(sqlUpdateSP);
            PreparedStatement pstCT = con.prepareStatement(sqlCT);

            for (int i = 0; i < model.getRowCount(); i++) {
                int maSP = Integer.parseInt(model.getValueAt(i, 0).toString());
                int sl = Integer.parseInt(model.getValueAt(i, 2).toString());
                double giaNhap = Double.parseDouble(model.getValueAt(i, 3).toString());
                double giaBanMoi = Double.parseDouble(model.getValueAt(i, 4).toString());
                double thanhTien = Double.parseDouble(model.getValueAt(i, 5).toString());

                // 1. Cập nhật bảng Sản Phẩm (Kho + Giá)
                pstSP.setInt(1, sl);
                pstSP.setDouble(2, giaBanMoi); // Cập nhật giá bán mới trực tiếp
                pstSP.setInt(3, maSP);
                pstSP.addBatch();

                // 2. Lưu chi tiết phiếu nhập
                pstCT.setInt(1, maPN);
                pstCT.setInt(2, maSP);
                pstCT.setInt(3, sl);
                pstCT.setDouble(4, giaNhap);
                pstCT.setDouble(5, thanhTien);
                pstCT.addBatch();
            }

            pstSP.executeBatch();
            pstCT.executeBatch();
            con.commit();

            JOptionPane.showMessageDialog(this, "Đã nhập hàng và cập nhật giá bán mới thành công!");
            resetTable();

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    public void resetTable() {
        model.setRowCount(0); // Xóa hết các dòng đang nhập dở
        lblTongTien.setText("Tổng cộng: 0 đ");
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}