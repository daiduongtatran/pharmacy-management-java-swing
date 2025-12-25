package com.pharmacy.Gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Hệ Thống Quản Lý Nhà Thuốc");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Thiết kế giao diện cơ bản ở đây
        setLayout(new BorderLayout());
        JLabel lblWelcome = new JLabel("CHƯƠNG TRÌNH QUẢN LÝ NHÀ THUỐC", JLabel.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblWelcome, BorderLayout.CENTER);
    }
}