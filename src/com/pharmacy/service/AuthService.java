package com.pharmacy.service;

public class AuthService {
    /**
     * Hàm kiểm tra đăng nhập với dữ liệu tĩnh
     * @param username Tên đăng nhập người dùng nhập vào
     * @param password Mật khẩu người dùng nhập vào
     * @return true nếu khớp admin/123, ngược lại false
     */
    public boolean verifyLogin(String username, String password) {
        // Kiểm tra dữ liệu tĩnh: tài khoản là "admin" và mật khẩu là "123"
        if ("admin".equals(username) && "123".equals(password)) {
            return true;
        }
        return false;
    }
}