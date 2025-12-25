package com.pharmacy.ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection con = null;
    private static Database instance = new Database();

    public static Database getInstance() {
        return instance;
    }

    public void connect() {
        // Thay đổi thông tin phù hợp với DB của bạn (SQL Server/MySQL)
        String url = "jdbc:sqlserver://localhost;"
                + "databaseName=Qlynhathuoc;"
                + "integratedSecurity=true;"
                + "trustServerCertificate=true";
        try {
            con = DriverManager.getConnection(url);
            System.out.println("Kết nối Database thành công!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return con;
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}