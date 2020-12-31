package com.zgyw.materiel.controller;

import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.User;
import com.zgyw.materiel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/user/login")
    public ResultVO login(HttpServletResponse response,
                          @RequestParam(name = "name") String name,
                          @RequestParam(name = "passWord") String passWord) throws NoSuchAlgorithmException,UnsupportedEncodingException {
        User result = userService.login(response, name, passWord);
        return ResultVO.success(result);
    }

    @PostMapping("/user/logout")
    public ResultVO logout(HttpServletResponse response, HttpServletRequest request) {
        userService.logout(response,request);
        return ResultVO.success();
    }
}
