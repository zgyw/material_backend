package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public interface UserService {
    User login(HttpServletResponse response, String name, String passWord) throws NoSuchAlgorithmException,UnsupportedEncodingException;

    void logout(HttpServletResponse response, HttpServletRequest request);
}
