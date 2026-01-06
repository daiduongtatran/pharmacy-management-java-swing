package com.pharmacy.gui;

import com.pharmacy.service.AuthService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private AuthService authService = new AuthService();

    public LoginFrame() {
        // Cấu hình cơ bản cho Frame
        setTitle("Hệ thống Quản lý Nhà thuốc - Đăng nhập");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel chính
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(null);

        // Tiêu đề
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 74, 153));
        lblTitle.setBounds(0, 40, 400, 40);
        mainPanel.add(lblTitle);

        // Ô nhập Tài khoản
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setBounds(50, 110, 300, 30);
        mainPanel.add(lblUser);

        JTextField txtUser = new JTextField();
        txtUser.setBounds(50, 140, 300, 35);
        mainPanel.add(txtUser);

        // Ô nhập Mật khẩu
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setBounds(50, 190, 300, 30);
        mainPanel.add(lblPass);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBounds(50, 220, 300, 35);
        mainPanel.add(txtPass);

        // Nút Đăng nhập
        JButton btnLogin = new JButton("Đăng Nhập");
        btnLogin.setBounds(50, 300, 300, 40);
        btnLogin.setBackground(new Color(0, 74, 153));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        mainPanel.add(btnLogin);

        // CHỖ SỬA 2: Xử lý sự kiện nút bấm (Không khai báo 'private' ở đây nữa)
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = txtUser.getText();
                String pass = new String(txtPass.getPassword());

                // Gọi hàm kiểm tra từ authService đã khai báo ở trên
                if (authService.verifyLogin(user, pass)) {
                    JOptionPane.showMessageDialog(null, "Đăng nhập thành công!");

                    dispose(); // Đóng cửa sổ đăng nhập

                    new MainFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}