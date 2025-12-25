package com.pharmacy;

import com.pharmacy.ConnectDB.Database;
import com.pharmacy.Gui.MainFrame;
import javax.swing.JOptionPane;

public class App {
    public static void main(String[] args) {
        //  kết nối Database
        try {
            Database.getInstance().connect();

            // Kiểm tra xem kết nối có thực sự thành công không
            if (Database.getConnection() != null) {
                // 2. Nếu OK, mở giao diện chính
                java.awt.EventQueue.invokeLater(() -> {
                    new MainFrame().setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(null, "Không thể kết nối Database! Vui lòng kiểm tra lại cấu hình.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}