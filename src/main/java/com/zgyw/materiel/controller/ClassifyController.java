package com.zgyw.materiel.controller;

import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.service.ClassifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClassifyController {

    @Autowired
    private ClassifyService service;

    @GetMapping("/classify/findAll")
    public ResultVO findAll() {
        List<Classify> result = service.findAll();
        return ResultVO.success(result);
    }
}
