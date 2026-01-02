package com.pharmacy;

import com.pharmacy.gui.LoginFrame;
import com.pharmacy.connectdb.Database;
import javax.swing.JOptionPane;

public class App {
    public static void main(String[] args) {
        try {
            Database.getInstance().connect();

            // Kiểm tra xem kết nối có thực sự thành công không
            if (Database.getConnection() != null) {
                // 2. Nếu OK, mở giao diện chính
                java.awt.EventQueue.invokeLater(() -> {
                    new LoginFrame().setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(null, "Không thể kết nối Database! Vui lòng kiểm tra lại cấu hình.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}