
package com.pharmacy.connectdb;

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
        String url = "jdbc:sqlserver://localhost;"
                + "databaseName=Quanlynhathuoc;"
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
        try {
            if (con == null || con.isClosed()) {
                instance.connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
