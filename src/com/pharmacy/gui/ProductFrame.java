package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class ProductFrame extends JPanel {
    private JTable tbl;
    private DefaultTableModel model;
    private JTextArea txtNote;
    private JTextField txtSearch;

    // Khai báo màu sắc đồng bộ với MainFrame
    private final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private final Color COLOR_BG = new Color(245, 245, 245);

    public ProductFrame() {
        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Tạo khoảng cách lề

        // --- 1. THANH CÔNG CỤ (TOP) ---
        add(createTopPanel(), BorderLayout.NORTH);

        // --- 2. BẢNG SẢN PHẨM (CENTER) ---
        add(createTablePanel(), BorderLayout.CENTER);

        // --- 3. CHI TIẾT SẢN PHẨM (RIGHT) ---
        add(createDetailPanel(), BorderLayout.EAST);
    }

    private JPanel createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        // Ô tìm kiếm
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.setOpaque(false);
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(0, 35));
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 35));

        pnlSearch.add(new JLabel("🔍 "));
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);

        // Nhóm nút chức năng
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setOpaque(false);
        pnlActions.add(createStyledButton("Thêm mới", new Color(40, 167, 69)));
        pnlActions.add(createStyledButton("Sửa", new Color(255, 193, 7)));
        pnlActions.add(createStyledButton("Xóa", new Color(220, 53, 69)));
        pnlActions.add(createStyledButton("Làm mới", COLOR_PRIMARY));

        pnlTop.add(pnlSearch, BorderLayout.WEST);
        pnlTop.add(pnlActions, BorderLayout.EAST);
        return pnlTop;
    }

    private JPanel createTablePanel() {
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);

        String[] headers = {"Mã SP", "Tên SP", "Loại", "Giá Nhập", "Giá Bán", "Xuất", "Tồn"};
        model = new DefaultTableModel(headers, 0);
        tbl = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };

        // Tùy chỉnh Table Header
        JTableHeader header = tbl.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        // Tùy chỉnh dòng trong bảng
        tbl.setRowHeight(35);
        tbl.setFont(new Font("Arial", Font.PLAIN, 13));
        tbl.setSelectionBackground(new Color(230, 240, 250));
        tbl.setGridColor(new Color(240, 240, 240));

        loadData();

        JScrollPane scrollPane = new JScrollPane(tbl);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        pnlTable.add(scrollPane, BorderLayout.CENTER);

        return pnlTable;
    }

    private JPanel createDetailPanel() {
        JPanel pnlRight     = new JPanel(new BorderLayout());
        pnlRight.setPreferredSize(new Dimension(300, 0));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel("CHI TIẾT SẢN PHẨM", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        txtNote = new JTextArea("\n  Chọn một sản phẩm từ bảng\n  để xem thông tin đầy đủ...");
        txtNote.setEditable(false);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNote.setBackground(new Color(252, 252, 252));

        pnlRight.add(lblTitle, BorderLayout.NORTH);
        pnlRight.add(new JScrollPane(txtNote), BorderLayout.CENTER);

        // Sự kiện khi click vào dòng trong bảng
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tbl.getSelectedRow() != -1) {
                updateDetailArea();
            }
        });

        return pnlRight;
    }

    private void updateDetailArea() {
        int row = tbl.getSelectedRow();
        String ten = model.getValueAt(row, 1).toString();
        String loai = model.getValueAt(row, 2).toString();
        String ton = model.getValueAt(row, 6).toString();

        String detailText = String.format(
                "📦 Tên SP: %s\n\n" +
                        "🏷 Loại: %s\n\n" +
                        "📊 Tồn kho: %s\n\n" +
                        "📝 Ghi chú: \n- Sản phẩm đạt chuẩn GPP\n- Bảo quản nơi khô ráo.\n- Hạn sử dụng xem trên bao bì.",
                ten, loai, ton
        );
        txtNote.setText(detailText);
        txtNote.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void loadData() {
        try (Connection con = Database.getConnection()) {
            if (con == null) return;
            model.setRowCount(0);
            String sql = "SELECT * FROM SanPham";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next()){
                Object[] row = {
                        rs.getInt("MaSP"),
                        rs.getString("TenSP"),
                        rs.getString("LoaiSP"),
                        rs.getDouble("GiaNhap"),
                        rs.getDouble("GiaBan"),
                        rs.getInt("HangXuat"),
                        rs.getInt("TonKho")
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}