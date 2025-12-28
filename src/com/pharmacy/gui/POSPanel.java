package com.pharmacy.gui;

import com.pharmacy.connectdb.Database;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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
    private int currentProductId = -1; // ID sản phẩm đang chọn
    private double currentPrice = 0;
    private int currentStock = 0;
    private double finalTotal = 0;

    // --- Styles ---
    private final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private final Color COLOR_DANGER = new Color(220, 53, 69);
    private final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);

    public POSPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // 1. Panel TRÁI: Tìm kiếm và Thông tin sản phẩm
        add(createSelectionPanel(), BorderLayout.WEST);

        // 2. Panel PHẢI: Giỏ hàng và Thanh toán
        add(createCartPanel(), BorderLayout.CENTER);
    }

    // --- KHU VỰC 1: TÌM KIẾM & CHỌN SẢN PHẨM ---
    private JPanel createSelectionPanel() {
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.setPreferredSize(new Dimension(320, 0));
        pnlLeft.setOpaque(false);

        // A. Ô Tìm kiếm
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 5));
        pnlSearch.setBorder(BorderFactory.createTitledBorder(null, "1. Tìm sản phẩm", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, COLOR_PRIMARY));
        pnlSearch.setBackground(Color.WHITE);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 35));
        JButton btnFind = new JButton("🔍");

        // Sự kiện tìm kiếm (Enter hoặc Click nút)
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) searchProduct();
            }
        });
        btnFind.addActionListener(e -> searchProduct());

        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(btnFind, BorderLayout.EAST);

        // B. Thông tin sản phẩm tìm được
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

        // C. Nút Thêm vào giỏ
        btnAddToCart = new JButton("THÊM VÀO GIỎ ⬇");
        styleButton(btnAddToCart, COLOR_PRIMARY);
        btnAddToCart.setPreferredSize(new Dimension(0, 50));
        btnAddToCart.setEnabled(false); // Chỉ bật khi tìm thấy thuốc
        btnAddToCart.addActionListener(e -> addToCart());

        // Gom lại vào panel trái
        JPanel container = new JPanel(new BorderLayout(10, 20));
        container.setOpaque(false);
        container.add(pnlSearch, BorderLayout.NORTH);
        container.add(pnlInfo, BorderLayout.CENTER);
        container.add(btnAddToCart, BorderLayout.SOUTH);

        pnlLeft.add(container, BorderLayout.NORTH);
        return pnlLeft;
    }

    // --- KHU VỰC 2: GIỎ HÀNG & THANH TOÁN ---
    private JPanel createCartPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout(10, 10));
        pnlRight.setOpaque(false);

        // A. Bảng giỏ hàng
        String[] headers = {"Mã SP", "Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Thành Tiền"};
        cartModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(35);
        tblCart.setFont(new Font("Arial", Font.PLAIN, 14));

        JTableHeader header = tblCart.getTableHeader();
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_BOLD);

        JScrollPane scroll = new JScrollPane(tblCart);
        scroll.setBorder(BorderFactory.createTitledBorder(null, "3. Giỏ hàng", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, COLOR_PRIMARY));
        scroll.getViewport().setBackground(Color.WHITE);

        // B. Khu vực Thanh toán (Dưới cùng)
        JPanel pnlBottom = new JPanel(new BorderLayout(20, 0));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Nút xóa dòng chọn
        btnDelete = new JButton("Xóa dòng chọn");
        styleButton(btnDelete, COLOR_DANGER);
        btnDelete.setPreferredSize(new Dimension(150, 40));
        btnDelete.addActionListener(e -> removeFromCart());

        // Tổng tiền
        lblTotalMoney = new JLabel("Tổng: 0 đ");
        lblTotalMoney.setFont(new Font("Arial", Font.BOLD, 26));
        lblTotalMoney.setForeground(COLOR_DANGER);
        lblTotalMoney.setHorizontalAlignment(SwingConstants.RIGHT);

        // Nút Thanh toán
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

    // --- LOGIC 1: TÌM SẢN PHẨM ---
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

                // Hiển thị lên giao diện
                lblName.setText("<html><body style='width: 150px'>" + name + "</body></html>");
                lblPrice.setText(formatMoney(currentPrice));
                lblStock.setText(String.valueOf(currentStock));
                lblName.setForeground(COLOR_PRIMARY);

                // Kích hoạt nút thêm
                btnAddToCart.setEnabled(true);
                spnQuantity.setValue(1);
                spnQuantity.requestFocus(); // Nhảy vào ô nhập số lượng luôn cho tiện
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm!");
                resetSelection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- LOGIC 2: THÊM VÀO GIỎ HÀNG ---
    private void addToCart() {
        int qty = (int) spnQuantity.getValue();

        // 1. Kiểm tra tồn kho
        if (qty > currentStock) {
            JOptionPane.showMessageDialog(this, "Không đủ hàng! Tồn kho chỉ còn: " + currentStock);
            return;
        }

        // 2. Kiểm tra xem sản phẩm đã có trong giỏ chưa
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int id = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
            if (id == currentProductId) {
                // Đã có -> Cộng dồn số lượng
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

        // 3. Chưa có -> Thêm dòng mới
        double lineTotal = qty * currentPrice;
        cartModel.addRow(new Object[]{
                currentProductId,
                lblName.getText().replaceAll("<[^>]*>", ""),
                formatMoney(currentPrice),
                qty,
                formatMoney(lineTotal) // Lưu dạng chuỗi hiển thị
        });

        calculateTotal();
        resetSelection();
        txtSearch.requestFocus(); // Trả con trỏ về ô tìm kiếm để bán tiếp
    }

    // --- LOGIC 3: THANH TOÁN & TRỪ KHO (TRANSACTION) ---
    private void processPayment() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
            return;
        }

        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false); // BẮT ĐẦU GIAO DỊCH (Transaction)

            // Bước 1: Tạo Hóa Đơn
            String sqlHD = "INSERT INTO HoaDon (NgayInHoaDon, TongTien, MaKH) VALUES (?, ?, ?)";
            PreparedStatement pstHD = con.prepareStatement(sqlHD, Statement.RETURN_GENERATED_KEYS);
            pstHD.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            pstHD.setDouble(2, finalTotal);
            pstHD.setObject(3, null);
            pstHD.executeUpdate();

            // Lấy ID hóa đơn vừa tạo
            ResultSet rsKey = pstHD.getGeneratedKeys();
            int maHD = 0;
            if (rsKey.next()) maHD = rsKey.getInt(1);

            // Bước 2: Trừ tồn kho & (Nếu muốn) Lưu chi tiết hóa đơn
            String sqlUpdateKho = "UPDATE SanPham SET TonKho = TonKho - ? WHERE MaSP = ?";
            PreparedStatement pstKho = con.prepareStatement(sqlUpdateKho);

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int maSP = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
                int soLuong = Integer.parseInt(cartModel.getValueAt(i, 3).toString());

                // Trừ kho
                pstKho.setInt(1, soLuong);
                pstKho.setInt(2, maSP);
                pstKho.addBatch();
            }
            pstKho.executeBatch(); // Thực thi một loạt lệnh update

            // Bước 3: Hoàn tất
            con.commit(); // Lưu vào DB
            JOptionPane.showMessageDialog(this, "Thanh toán thành công!\nMã Hóa Đơn: " + maHD + "\nTổng tiền: " + lblTotalMoney.getText());

            // Làm mới giao diện
            cartModel.setRowCount(0);
            calculateTotal();

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ex) {} // Gặp lỗi thì hoàn tác
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + e.getMessage());
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (Exception ex) {}
        }
    }

    // --- CÁC HÀM PHỤ TRỢ ---
    private void calculateTotal() {
        finalTotal = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            // Lấy thành tiền (cần parse lại từ chuỗi định dạng "1,000,000")
            String sAmount = cartModel.getValueAt(i, 4).toString().replace(",", "").replace(".", "");
            finalTotal += Double.parseDouble(sAmount);
        }
        lblTotalMoney.setText("Tổng: " + formatMoney(finalTotal) + " đ");
    }

    private void removeFromCart() {
        int row = tblCart.getSelectedRow();
        if (row != -1) {
            cartModel.removeRow(row);
            calculateTotal();
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