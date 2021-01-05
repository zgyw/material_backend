package com.zgyw.materiel.controller;

import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.service.ClassifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
public class ClassifyController {

    @Autowired
    private ClassifyService service;

    @GetMapping("/classify/findAll")
    public ResultVO findAll() {
        List<Classify> result = service.findAll();
        return ResultVO.success(result);
    }

    @PostMapping("/classify/add")
    public ResultVO add(@Valid Classify classify, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【新增分类】参数不正确，classify={}", classify);
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        Classify result = service.add(classify);
        return ResultVO.success(result);
    }

    @PostMapping("/classify/modify")
    public ResultVO modify(@Valid Classify classify, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【修改分类】参数不正确，classify={}", classify);
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        Classify result = service.modify(classify);
        return ResultVO.success(result);
    }

    @GetMapping("/classify/detail")
    public ResultVO detail(@RequestParam(name = "id")Integer id) {
        Classify result = service.detail(id);
        return ResultVO.success(result);
    }

    @GetMapping("/classify/delete")
    public ResultVO delete(@RequestParam(name = "id")Integer id) {
        Classify result = service.detail(id);
        return ResultVO.success();
    }
}
