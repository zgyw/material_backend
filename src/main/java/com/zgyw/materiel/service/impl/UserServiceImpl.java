package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.User;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.repository.UserRepository;
import com.zgyw.materiel.service.UserService;
import com.zgyw.materiel.util.CookieUtil;
import com.zgyw.materiel.util.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    public User login(HttpServletResponse response, String name, String passWord) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        User user = repository.findByLoginName(name);
        if (user == null) {
            throw new MTException(ResultEnum.EMPTY_USER);
        }
        if (!user.getPassWord().equals(EncryptUtil.encodeByMd5(passWord))) {
            throw new MTException(ResultEnum.USER_ERROR);
        }
        String uuid = UUID.randomUUID().toString();
        CookieUtil.set(response,"uuid",uuid,28800);
        return user;
    }

    @Override
    public void logout(HttpServletResponse response, HttpServletRequest request) {
        Cookie uuid = CookieUtil.get(request, "uuid");
        if (uuid != null) {
            CookieUtil.set(response,"uuid",null,0);
        }
    }
}
