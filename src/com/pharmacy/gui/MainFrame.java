package com.pharmacy.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel pnlMain; // Vùng hiển thị nội dung trắng

    public MainFrame() {
        setTitle("Hệ Thống Quản Lý Nhà Thuốc");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. THANH MENU BÊN TRÁI (XANH ĐẬM)
        JPanel pnlLeft = new JPanel();
        pnlLeft.setBackground(new Color(13, 71, 161)); // Xanh đậm
        pnlLeft.setPreferredSize(new Dimension(200, 0));
        pnlLeft.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20));

        JButton btnProduct = new JButton("Sản phẩm & Kho");
        btnProduct.setPreferredSize(new Dimension(180, 40));
        btnProduct.setFocusPainted(false);
        pnlLeft.add(btnProduct);

        // 2. VÙNG NỘI DUNG CHÍNH (TRẮNG)
        pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(Color.WHITE);
        JLabel lblEmpty = new JLabel("Chọn một chức năng từ menu bên trái", JLabel.CENTER);
        pnlMain.add(lblEmpty);

        // 3. XỬ LÝ SỰ KIỆN BẤM NÚT
        btnProduct.addActionListener(e -> {
            pnlMain.removeAll(); // Xóa màn hình chào mừng
            pnlMain.add(new ProductFrame(), BorderLayout.CENTER); // Thêm bảng sản phẩm
            pnlMain.revalidate();
            pnlMain.repaint();
        });

        add(pnlLeft, BorderLayout.WEST);
        add(pnlMain, BorderLayout.CENTER);
    }
}