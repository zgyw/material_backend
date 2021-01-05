package com.zgyw.materiel.bean;


import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.service.TGroupService;
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
public class TGroupController {
    @Autowired
    private TGroupService service;

    @PostMapping("/group/add")
    public ResultVO add(@Valid TGroup tGroup, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【新增分组】参数不正确，tGroup={}", tGroup);
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        TGroup result = service.add(tGroup);
        return ResultVO.success(result);
    }

    @GetMapping("/group/findAll")
    public ResultVO findAll() {
        List<TGroup> result = service.findAll();
        return ResultVO.success(result);
    }

    @GetMapping("/group/detail")
    public ResultVO detail(@RequestParam(name = "id")Integer id) {
        TGroup result = service.detail(id);
        return ResultVO.success(result);
    }

    @PostMapping("/group/modify")
    public ResultVO modify(@Valid TGroup tGroup, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【修改分组】参数不正确，tGroup={}", tGroup);
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        TGroup result = service.modify(tGroup);
        return ResultVO.success(result);
    }

    @GetMapping("/group/delete")
    public ResultVO delete(@RequestParam(name = "id")Integer id) {
        service.delete(id);
        return ResultVO.success();
    }

    @GetMapping("/group/getClassify")
    public ResultVO getClassify(@RequestParam(name = "id")Integer id) {
        List<Classify> result = service.getClassify(id);
        return ResultVO.success(result);
    }
}
