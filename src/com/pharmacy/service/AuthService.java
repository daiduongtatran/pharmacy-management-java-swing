package com.pharmacy.service;

public class AuthService {
    public boolean verifyLogin(String username, String password) {
        // Kiểm tra dữ liệu tĩnh: tài khoản là "admin" và mật khẩu là "123"
        if ("admin".equals(username) && "123".equals(password)) {
            return true;
        }
        return false;
    }
}