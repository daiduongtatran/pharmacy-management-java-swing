package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class POSPanel extends JPanel {

    // --- UI Components ---
    private JTextField txtSearch;
    private JLabel lblName, lblStock, lblPrice;
    private JSpinner spnQuantity;
    private JButton btnAddToCart, btnPay, btnDelete;
    private JTable tblCart;
    private DefaultTableModel cartModel;
    private JLabel lblTotalMoney;

    // --- Data Variables ---
    private int currentProductId = -1;
    private double currentPrice = 0;
    private int currentStock = 0;
    private double finalTotal = 0;
    private java.sql.Date currentExpiryDate = null;
    // --- Styles ---
    private final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private final Color COLOR_DANGER = new Color(220, 53, 69);
    private final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);

    public POSPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        add(createSelectionPanel(), BorderLayout.WEST);
        add(createCartPanel(), BorderLayout.CENTER);
    }

    private JPanel createSelectionPanel() {
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.setPreferredSize(new Dimension(320, 0));
        pnlLeft.setOpaque(false);

        JPanel pnlSearch = new JPanel(new BorderLayout(5, 5));
        pnlSearch.setBorder(BorderFactory.createTitledBorder(null, "1. Tìm sản phẩm", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, COLOR_PRIMARY));
        pnlSearch.setBackground(Color.WHITE);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 35));
        JButton btnFind = new JButton("🔍");

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) searchProduct();
            }
        });
        btnFind.addActionListener(e -> searchProduct());

        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(btnFind, BorderLayout.EAST);

        JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 10, 15));
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(20, 0, 0, 0),
                BorderFactory.createTitledBorder(null, "2. Thông tin & Số lượng", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, COLOR_PRIMARY)
        ));
        pnlInfo.setBackground(Color.WHITE);

        lblName = new JLabel("---");
        lblPrice = new JLabel("0 đ");
        lblStock = new JLabel("0");
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

        pnlInfo.add(new JLabel("Tên thuốc:")); pnlInfo.add(lblName);
        pnlInfo.add(new JLabel("Đơn giá:"));   pnlInfo.add(lblPrice);
        pnlInfo.add(new JLabel("Tồn kho:"));   pnlInfo.add(lblStock);
        pnlInfo.add(new JLabel("Số lượng mua:")); pnlInfo.add(spnQuantity);

        btnAddToCart = new JButton("THÊM VÀO GIỎ ");
        styleButton(btnAddToCart, COLOR_PRIMARY);
        btnAddToCart.setPreferredSize(new Dimension(0, 50));
        btnAddToCart.setEnabled(false);
        btnAddToCart.addActionListener(e -> addToCart());

        JPanel container = new JPanel(new BorderLayout(10, 20));
        container.setOpaque(false);
        container.add(pnlSearch, BorderLayout.NORTH);
        container.add(pnlInfo, BorderLayout.CENTER);
        container.add(btnAddToCart, BorderLayout.SOUTH);

        pnlLeft.add(container, BorderLayout.NORTH);
        return pnlLeft;
    }

    private JPanel createCartPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout(10, 10));
        pnlRight.setOpaque(false);

        String[] headers = {"Mã SP", "Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Thành Tiền"};
        cartModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(35);
        tblCart.setFont(new Font("Arial", Font.PLAIN, 14));

        // Căn lề giữa cho bảng giỏ hàng
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblCart.getColumnCount(); i++) {
            tblCart.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = tblCart.getTableHeader();
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_BOLD);

        JScrollPane scroll = new JScrollPane(tblCart);
        scroll.setBorder(BorderFactory.createTitledBorder(null, "3. Giỏ hàng", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, COLOR_PRIMARY));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlBottom = new JPanel(new BorderLayout(20, 0));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- THIẾT KẾ MỚI CHO NÚT XÓA ---
        btnDelete = new JButton(" Xóa dòng chọn");
        btnDelete.setFont(new Font("Arial", Font.PLAIN, 13));
        btnDelete.setFocusPainted(false);
        btnDelete.setContentAreaFilled(false);
        btnDelete.setOpaque(true);
        btnDelete.setBackground(new Color(245, 245, 245));
        btnDelete.setForeground(new Color(200, 0, 0));
        btnDelete.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.setPreferredSize(new Dimension(150, 40));

        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnDelete.setBackground(new Color(255, 235, 235));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnDelete.setBackground(new Color(245, 245, 245));
            }
        });
        btnDelete.addActionListener(e -> removeFromCart());

        lblTotalMoney = new JLabel("Tổng: 0 đ");
        lblTotalMoney.setFont(new Font("Arial", Font.BOLD, 26));
        lblTotalMoney.setForeground(COLOR_DANGER);
        lblTotalMoney.setHorizontalAlignment(SwingConstants.RIGHT);

        btnPay = new JButton("THANH TOÁN (LƯU & IN)");
        styleButton(btnPay, COLOR_SUCCESS);
        btnPay.setFont(new Font("Arial", Font.BOLD, 16));
        btnPay.setPreferredSize(new Dimension(250, 50));
        btnPay.addActionListener(e -> processPayment());

        JPanel pnlTotal = new JPanel(new BorderLayout(10, 0));
        pnlTotal.setOpaque(false);
        pnlTotal.add(lblTotalMoney, BorderLayout.NORTH);
        pnlTotal.add(btnPay, BorderLayout.CENTER);

        pnlBottom.add(btnDelete, BorderLayout.WEST);
        pnlBottom.add(pnlTotal, BorderLayout.EAST);

        pnlRight.add(scroll, BorderLayout.CENTER);
        pnlRight.add(pnlBottom, BorderLayout.SOUTH);

        return pnlRight;
    }

    private void searchProduct() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) return;

        try (Connection con = Database.getConnection()) {
            String sql = "SELECT * FROM SanPham WHERE MaSP = ? OR TenSP LIKE ?";
            PreparedStatement pst = con.prepareStatement(sql);
            try {
                pst.setInt(1, Integer.parseInt(keyword));
            } catch (NumberFormatException e) {
                pst.setInt(1, -1);
            }
            pst.setString(2, "%" + keyword + "%");

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                currentProductId = rs.getInt("MaSP");
                String name = rs.getString("TenSP");
                currentPrice = rs.getDouble("GiaBan");
                currentStock = rs.getInt("TonKho");

                // [MỚI] Lưu ngày hết hạn vào biến tạm, CHƯA kiểm tra ngay
                currentExpiryDate = rs.getDate("HanDung");

                // Hiển thị thông tin lên giao diện ngay lập tức
                lblName.setText("<html><body style='width: 150px'>" + name + "</body></html>");
                lblPrice.setText(formatMoney(currentPrice) + " đ");
                lblStock.setText(String.valueOf(currentStock));
                lblName.setForeground(COLOR_PRIMARY);

                btnAddToCart.setEnabled(true);
                spnQuantity.setValue(1);
                spnQuantity.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm!");
                resetSelection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToCart() {
        if (currentExpiryDate != null) {
            LocalDate expiry = currentExpiryDate.toLocalDate();
            if (expiry.isBefore(LocalDate.now())) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                JOptionPane.showMessageDialog(this,
                        "KHÔNG THỂ BÁN!\nSản phẩm này đã hết hạn ngày: " + sdf.format(currentExpiryDate),
                        "Cảnh báo hết hạn",
                        JOptionPane.WARNING_MESSAGE);
                return; // Dừng lại, không thêm vào giỏ
            }
        }
        int qty = (int) spnQuantity.getValue();
        if (qty > currentStock) {
            JOptionPane.showMessageDialog(this, "Không đủ hàng! Tồn kho chỉ còn: " + currentStock);
            return;
        }

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int id = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
            if (id == currentProductId) {
                int oldQty = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                int newQty = oldQty + qty;
                if (newQty > currentStock) {
                    JOptionPane.showMessageDialog(this, "Tổng số lượng trong giỏ vượt quá tồn kho!");
                    return;
                }
                cartModel.setValueAt(newQty, i, 3);
                cartModel.setValueAt(formatMoney(newQty * currentPrice), i, 4);
                calculateTotal();
                resetSelection();
                return;
            }
        }

        double lineTotal = qty * currentPrice;
        cartModel.addRow(new Object[]{
                currentProductId,
                lblName.getText().replaceAll("<[^>]*>", ""),
                formatMoney(currentPrice),
                qty,
                formatMoney(lineTotal)
        });

        calculateTotal();
        resetSelection();
        txtSearch.requestFocus();
    }

    private void processPayment() {
        if (cartModel.getRowCount() == 0) return;
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            String sqlHD = "INSERT INTO HoaDon (NgayInHoaDon, TongTien) VALUES (?, ?)";
            PreparedStatement pstHD = con.prepareStatement(sqlHD, Statement.RETURN_GENERATED_KEYS);
            pstHD.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            pstHD.setDouble(2, finalTotal);
            pstHD.executeUpdate();

            ResultSet rs = pstHD.getGeneratedKeys();
            int maHD = rs.next() ? rs.getInt(1) : 0;

            String sqlCT = "INSERT INTO ChiTietHoaDon (MaHD, MaSP, SoLuong, DonGia, ThanhTien) VALUES (?,?,?,?,?)";
            String sqlKho = "UPDATE SanPham SET TonKho = TonKho - ?, HangXuat = HangXuat + ? WHERE MaSP = ?";
            PreparedStatement pstCT = con.prepareStatement(sqlCT);
            PreparedStatement pstKho = con.prepareStatement(sqlKho);

            String sqlGetGiaGoc = "SELECT GiaNhap FROM SanPham WHERE MaSP = ?";
            String sqlLoiNhuan = "INSERT INTO LoiNhuan (MaHD, MaSP, SoLuongXuat, GiaGocSP, GiaBanSP) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstGetGia = con.prepareStatement(sqlGetGiaGoc);
            PreparedStatement pstLoiNhuan = con.prepareStatement(sqlLoiNhuan);
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int id = (int) cartModel.getValueAt(i, 0);
                int q = (int) cartModel.getValueAt(i, 3);
                double p = Double.parseDouble(cartModel.getValueAt(i, 2).toString().replaceAll("[^0-9]", ""));
                double sub = Double.parseDouble(cartModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));

                double giaNhap = 0;
                pstGetGia.setInt(1, id);
                ResultSet rsGia = pstGetGia.executeQuery();
                if(rsGia.next()) giaNhap = rsGia.getDouble("GiaNhap");
                rsGia.close();

                pstLoiNhuan.setInt(1, maHD);
                pstLoiNhuan.setInt(2, id);
                pstLoiNhuan.setInt(3, q);
                pstLoiNhuan.setDouble(4, giaNhap); // Lưu giá nhập thực tế (ví dụ 100đ)
                pstLoiNhuan.setDouble(5, p);       // Lưu giá bán (ví dụ 30,000đ)
                pstLoiNhuan.addBatch();

                pstCT.setInt(1, maHD); pstCT.setInt(2, id); pstCT.setInt(3, q); pstCT.setDouble(4, p); pstCT.setDouble(5, sub);
                pstCT.addBatch();

                pstKho.setInt(1, q); pstKho.setInt(2, q); pstKho.setInt(3, id);
                pstKho.addBatch();
            }

// Thực thi tất cả
            pstLoiNhuan.executeBatch(); // Chạy lệnh lưu lợi nhuận
            pstCT.executeBatch();
            pstKho.executeBatch();
            con.commit();
            JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
            cartModel.setRowCount(0);
            calculateTotal();
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + e.getMessage());
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (Exception ex) {}
        }
    }

    private void calculateTotal() {
        finalTotal = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            try {
                // Xóa mọi ký tự không phải số để tránh lỗi parse trên 1 triệu
                String sAmount = cartModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", "");
                if (!sAmount.isEmpty()) {
                    finalTotal += Double.parseDouble(sAmount);
                }
            } catch (Exception e) {
                System.err.println("Lỗi tính tiền: " + e.getMessage());
            }
        }
        lblTotalMoney.setText("Tổng: " + formatMoney(finalTotal) + " đ");
    }

    private void removeFromCart() {
        int row = tblCart.getSelectedRow();
        if (row != -1) {
            String tenSP = cartModel.getValueAt(row, 1).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa [" + tenSP + "] khỏi giỏ hàng?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                cartModel.removeRow(row);
                calculateTotal();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa!");
        }
    }

    private void resetSelection() {
        txtSearch.setText("");
        lblName.setText("---");
        lblPrice.setText("0 đ");
        lblStock.setText("0");
        spnQuantity.setValue(1);
        btnAddToCart.setEnabled(false);
        currentProductId = -1;
    }

    private String formatMoney(double amount) {
        return new DecimalFormat("#,###").format(amount);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}