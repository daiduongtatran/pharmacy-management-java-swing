package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductFrame extends JPanel {
    private JTable tbl;
    private DefaultTableModel model;
    private JTextArea txtNote;

    private final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private final Color COLOR_BG = new Color(245, 245, 245);

    public ProductFrame() {
        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createDetailPanel(), BorderLayout.EAST);
    }

    private JPanel createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        // Nút chức năng (Nằm bên phải)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setOpaque(false);

        JButton btnAdd = createStyledButton("Nhập hàng", new Color(40, 167, 69));
        JButton btnEdit = createStyledButton("Sửa", new Color(255, 193, 7));
        JButton btnDelete = createStyledButton("Xóa", new Color(220, 53, 69));
        // Đã xóa nút Làm mới

        // Gán sự kiện CRUD
        btnAdd.addActionListener(e -> openProductDialog(null));
        btnEdit.addActionListener(e -> {
            int selectedRow = tbl.getSelectedRow();
            if (selectedRow != -1) {
                openProductDialog(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để sửa!");
            }
        });
        btnDelete.addActionListener(e -> handleDelete());

        // Thêm nút vào Panel
        pnlActions.add(btnAdd);
        pnlActions.add(btnEdit);
        pnlActions.add(btnDelete);

        // Chỉ add panel Actions vào bên phải (EAST), bên trái để trống
        pnlTop.add(pnlActions, BorderLayout.EAST);

        return pnlTop;
    }

    // --- BẢNG MINI (DIALOG) ĐỂ NHẬP LIỆU ---
    private void openProductDialog(Integer rowIndex) {
        String title = (rowIndex == null) ? "Thêm Sản Phẩm Mới" : "Sửa Thông Tin Sản Phẩm";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel pnlInputs = new JPanel(new GridLayout(6, 2, 10, 10));
        pnlInputs.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField tfTen = new JTextField();
        JTextField tfLoai = new JTextField();
        JTextField tfGiaN = new JTextField();
        JTextField tfGiaB = new JTextField();
        JTextField tfSoLuong = new JTextField();
        JTextField tfHanDung = new JTextField();

        pnlInputs.add(new JLabel("Tên sản phẩm:")); pnlInputs.add(tfTen);
        pnlInputs.add(new JLabel("Loại sản phẩm:")); pnlInputs.add(tfLoai);
        pnlInputs.add(new JLabel("Giá nhập:")); pnlInputs.add(tfGiaN);
        pnlInputs.add(new JLabel("Giá bán:")); pnlInputs.add(tfGiaB);
        pnlInputs.add(new JLabel("Số lượng nhập:")); pnlInputs.add(tfSoLuong);
        pnlInputs.add(new JLabel("Hạn dùng (ngày/tháng/năm):")); pnlInputs.add(tfHanDung);

        // Nếu là sửa, đổ dữ liệu cũ vào các ô text
        if (rowIndex != null) {
            tfTen.setText(model.getValueAt(rowIndex, 1).toString());
            tfLoai.setText(model.getValueAt(rowIndex, 2).toString());

            // --- XỬ LÝ QUAN TRỌNG: Lọc bỏ chữ "đ" và dấu "," để lấy số nguyên ---
            String giaNhapRaw = model.getValueAt(rowIndex, 3).toString().replaceAll("[^0-9]", "");
            String giaBanRaw = model.getValueAt(rowIndex, 4).toString().replaceAll("[^0-9]", "");

            tfGiaN.setText(giaNhapRaw);
            tfGiaB.setText(giaBanRaw);
            // ---------------------------------------------------------------------

            tfSoLuong.setText(model.getValueAt(rowIndex, 6).toString());
            tfHanDung.setText(model.getValueAt(rowIndex, 7).toString());
        }

        JButton btnSave = new JButton("Lưu dữ liệu");
        btnSave.setPreferredSize(new Dimension(0, 40));
        btnSave.setBackground(COLOR_PRIMARY);
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> {
            try {
                if(tfTen.getText().isEmpty() || tfSoLuong.getText().isEmpty() || tfHanDung.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin (Tên, SL, Hạn dùng)!");
                    return;
                }

                handleSave(rowIndex, tfTen.getText(), tfLoai.getText(), tfGiaN.getText(), tfGiaB.getText(), tfSoLuong.getText(), tfHanDung.getText());
                dialog.dispose();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi nhập liệu: " + ex.getMessage());
            }
        });

        dialog.add(pnlInputs, BorderLayout.CENTER);
        dialog.add(btnSave, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleSave(Integer rowIndex, String ten, String loai, String giaN, String giaB, String soLuongNhap, String hanDungStr) throws SQLException {
        try (Connection con = Database.getConnection()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);

            long ms;
            try {
                java.util.Date dateUtil = sdf.parse(hanDungStr);
                ms = dateUtil.getTime();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ngày hết hạn không hợp lệ!\nVui lòng nhập đúng định dạng: ngày/tháng/năm (VD: 31/12/2025)");
                return;
            }
            java.sql.Date sqlDate = new java.sql.Date(ms);

            String sql;
            PreparedStatement ps;

            double dGiaNhap = Double.parseDouble(giaN);
            double dGiaBan = Double.parseDouble(giaB);
            int iSoLuong = Integer.parseInt(soLuongNhap);

            if (rowIndex == null) { // Thêm mới
                sql = "INSERT INTO SanPham (TenSP, LoaiSP, GiaNhap, GiaBan, HangXuat, TonKho, HanDung) VALUES (?, ?, ?, ?, 0, ?, ?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, ten);
                ps.setString(2, loai);
                ps.setDouble(3, dGiaNhap);
                ps.setDouble(4, dGiaBan);
                ps.setInt(5, iSoLuong);
                ps.setDate(6, sqlDate);
            } else { // Sửa
                sql = "UPDATE SanPham SET TenSP=?, LoaiSP=?, GiaNhap=?, GiaBan=?, TonKho=?, HanDung=? WHERE MaSP=?";
                ps = con.prepareStatement(sql);
                ps.setString(1, ten);
                ps.setString(2, loai);
                ps.setDouble(3, dGiaNhap);
                ps.setDouble(4, dGiaBan);
                ps.setInt(5, iSoLuong);
                ps.setDate(6, sqlDate);
                ps.setInt(7, (int) model.getValueAt(rowIndex, 0));
            }

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thao tác thành công!");
        }
    }

    private void handleDelete() {
        int row = tbl.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Hãy chọn sản phẩm muốn xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int maSP = (int) model.getValueAt(row, 0);
            try (Connection con = Database.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM SanPham WHERE MaSP = ?");
                ps.setInt(1, maSP);
                ps.executeUpdate();
                loadData();
            } catch (Exception e) {
                // Xử lý lỗi khóa ngoại nếu sản phẩm đã bán
                if (e.getMessage().contains("REFERENCE constraint")) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa sản phẩm này vì đã có lịch sử giao dịch!", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    // --- CÁC PHƯƠNG THỨC GIAO DIỆN BẢNG ---
    private JPanel createTablePanel() {
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);

        String[] headers = {"Mã SP", "Tên SP", "Loại", "Giá Nhập", "Giá Bán", "Xuất", "Tồn", "Hạn Dùng"};
        model = new DefaultTableModel(headers, 0);
        tbl = new JTable(model) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTableHeader header = tbl.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        tbl.setRowHeight(35);
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbl.getSelectedRow() != -1) updateDetailArea();
        });

        loadData();
        pnlTable.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return pnlTable;
    }

    private JPanel createDetailPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setPreferredSize(new Dimension(300, 0));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)), new EmptyBorder(10, 10, 10, 10)));
        JLabel lblTitle = new JLabel("CHI TIẾT SẢN PHẨM", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(COLOR_PRIMARY);
        txtNote = new JTextArea("\n  Chọn một sản phẩm...");
        txtNote.setEditable(false);
        txtNote.setLineWrap(true);
        txtNote.setBackground(new Color(252, 252, 252));
        pnlRight.add(lblTitle, BorderLayout.NORTH);
        pnlRight.add(new JScrollPane(txtNote), BorderLayout.CENTER);
        return pnlRight;
    }

    private void updateDetailArea() {
        int row = tbl.getSelectedRow();
        txtNote.setText(String.format("📦 Tên SP: %s\n🏷 Loại: %s\n📊 Tồn: %s\n⏳ Hạn dùng: %s",
                model.getValueAt(row, 1),
                model.getValueAt(row, 2),
                model.getValueAt(row, 6),
                model.getValueAt(row, 7)));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(110, 35));
        return btn;
    }

    // Nhớ thêm import java.text.DecimalFormat; ở đầu file nếu chưa có
    public void loadData() {
        try (Connection con = Database.getConnection()) {
            if (con == null) return;
            model.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM SanPham");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            // Thêm bộ định dạng tiền tệ
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");

            while(rs.next()){
                Date dateSQL = rs.getDate("HanDung");
                String hienThiNgay = (dateSQL != null) ? sdf.format(dateSQL) : "";

                // Định dạng giá tiền
                String giaNhapStr = df.format(rs.getDouble("GiaNhap")) + " đ";
                String giaBanStr = df.format(rs.getDouble("GiaBan")) + " đ";

                model.addRow(new Object[]{
                        rs.getInt("MaSP"),
                        rs.getString("TenSP"),
                        rs.getString("LoaiSP"),
                        giaNhapStr, // Hiển thị chuỗi đã format (Ví dụ: 20,000 đ)
                        giaBanStr,  // Hiển thị chuỗi đã format
                        rs.getInt("HangXuat"),
                        rs.getInt("TonKho"),
                        hienThiNgay
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}