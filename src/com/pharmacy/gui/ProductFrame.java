package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class ProductFrame extends JPanel {
    private JTable tbl;
    private DefaultTableModel model;
    private JTextArea txtNote;
    private JTextField txtSearch;

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

        // Tìm kiếm
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.setOpaque(false);
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(0, 35));
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 35));

        pnlSearch.add(new JLabel("🔍 "));
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);

        // Nút chức năng
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setOpaque(false);

        JButton btnAdd = createStyledButton("Thêm mới", new Color(40, 167, 69));
        JButton btnEdit = createStyledButton("Sửa", new Color(255, 193, 7));
        JButton btnDelete = createStyledButton("Xóa", new Color(220, 53, 69));
        JButton btnRefresh = createStyledButton("Làm mới", COLOR_PRIMARY);

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
        btnRefresh.addActionListener(e -> loadData());

        pnlActions.add(btnAdd);
        pnlActions.add(btnEdit);
        pnlActions.add(btnDelete);
        pnlActions.add(btnRefresh);

        pnlTop.add(pnlSearch, BorderLayout.WEST);
        pnlTop.add(pnlActions, BorderLayout.EAST);
        return pnlTop;
    }

    // --- BẢNG MINI (DIALOG) ĐỂ NHẬP LIỆU ---
    private void openProductDialog(Integer rowIndex) {
        String title = (rowIndex == null) ? "Thêm Sản Phẩm" : "Sửa Sản Phẩm";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel pnlInputs = new JPanel(new GridLayout(6, 2, 10, 10));
        pnlInputs.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField tfTen = new JTextField();
        JTextField tfLoai = new JTextField();
        JTextField tfGiaN = new JTextField();
        JTextField tfGiaB = new JTextField();
        JTextField tfHangX = new JTextField();
        JTextField tfTon = new JTextField();

        pnlInputs.add(new JLabel("Tên sản phẩm:")); pnlInputs.add(tfTen);
        pnlInputs.add(new JLabel("Loại sản phẩm:")); pnlInputs.add(tfLoai);
        pnlInputs.add(new JLabel("Giá nhập:")); pnlInputs.add(tfGiaN);
        pnlInputs.add(new JLabel("Giá bán:")); pnlInputs.add(tfGiaB);
        pnlInputs.add(new JLabel("Hàng xuất:")); pnlInputs.add(tfHangX);
        pnlInputs.add(new JLabel("Tồn kho:")); pnlInputs.add(tfTon);

        // Nếu là sửa, đổ dữ liệu cũ vào các ô text
        if (rowIndex != null) {
            tfTen.setText(model.getValueAt(rowIndex, 1).toString());
            tfLoai.setText(model.getValueAt(rowIndex, 2).toString());
            tfGiaN.setText(model.getValueAt(rowIndex, 3).toString());
            tfGiaB.setText(model.getValueAt(rowIndex, 4).toString());
            tfHangX.setText(model.getValueAt(rowIndex, 5).toString());
            tfTon.setText(model.getValueAt(rowIndex, 6).toString());
        }

        JButton btnSave = new JButton("Lưu dữ liệu");
        btnSave.setPreferredSize(new Dimension(0, 40));
        btnSave.setBackground(COLOR_PRIMARY);
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> {
            try {
                handleSave(rowIndex, tfTen.getText(), tfLoai.getText(), tfGiaN.getText(), tfGiaB.getText(), tfHangX.getText(), tfTon.getText());
                dialog.dispose();
                loadData(); // Cập nhật lại bảng chính
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage());
            }
        });

        dialog.add(pnlInputs, BorderLayout.CENTER);
        dialog.add(btnSave, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleSave(Integer rowIndex, String ten, String loai, String giaN, String giaB, String hangX, String ton) throws SQLException {
        try (Connection con = Database.getConnection()) {
            String sql;
            if (rowIndex == null) { // Thêm mới
                sql = "INSERT INTO SanPham (TenSP, LoaiSP, GiaNhap, GiaBan, HangXuat, TonKho) VALUES (?, ?, ?, ?, ?, ?)";
            } else { // Sửa
                sql = "UPDATE SanPham SET TenSP=?, LoaiSP=?, GiaNhap=?, GiaBan=?, HangXuat=?, TonKho=? WHERE MaSP=?";
            }

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ten);
            ps.setString(2, loai);
            ps.setDouble(3, Double.parseDouble(giaN));
            ps.setDouble(4, Double.parseDouble(giaB));
            ps.setInt(5, Integer.parseInt(hangX));
            ps.setInt(6, Integer.parseInt(ton));

            if (rowIndex != null) {
                ps.setInt(7, (int) model.getValueAt(rowIndex, 0)); // Lấy MaSP từ cột 0
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
                e.printStackTrace();
            }
        }
    }

    // --- CÁC PHƯƠNG THỨC GIAO DIỆN BẢNG (GIỮ NGUYÊN TỪ FILE CŨ) ---
    private JPanel createTablePanel() {
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        String[] headers = {"Mã SP", "Tên SP", "Loại", "Giá Nhập", "Giá Bán", "Xuất", "Tồn"};
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
        txtNote.setText(String.format("📦 Tên SP: %s\n🏷 Loại: %s\n📊 Tồn: %s",
                model.getValueAt(row, 1), model.getValueAt(row, 2), model.getValueAt(row, 6)));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setPreferredSize(new Dimension(110, 35));
        return btn;
    }

    public void loadData() {
        try (Connection con = Database.getConnection()) {
            if (con == null) return;
            model.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM SanPham");
            while(rs.next()){
                model.addRow(new Object[]{rs.getInt("MaSP"), rs.getString("TenSP"), rs.getString("LoaiSP"), rs.getDouble("GiaNhap"), rs.getDouble("GiaBan"), rs.getInt("HangXuat"), rs.getInt("TonKho")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}