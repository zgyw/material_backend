package com.zgyw.materiel.controller;


import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.OrderRecords;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.form.OrderRecordsForm;
import com.zgyw.materiel.service.OrderRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class OrderRecordsController {

    @Autowired
    private OrderRecordsService service;

    @GetMapping("/orderRecords/findByType")
    public ResultVO findByType (@RequestParam(name = "type") Integer type) {
        List<OrderRecords> result = service.findByType(type);
        return ResultVO.success(result);
    }

    @GetMapping("/orderRecords/pageList")
    public ResultVO pageList (@RequestParam(name = "type") Integer type,
                              @RequestParam(name = "status") Integer status,
                              @RequestParam(name = "content",defaultValue = "") String content,
                              @RequestParam(name = "page",defaultValue = "0") Integer page,
                              @RequestParam(name = "size",defaultValue = "10") Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC,"inTime","outTime");
        PageRequest pageRequest = PageRequest.of(page, size,sort);
        Map<String, Object> result = service.pageList(type, status, content, pageRequest);
        return ResultVO.success(result);
    }

    @PostMapping("/orderRecords/saveOrder")
    public ResultVO saveOrder(@Valid OrderRecordsForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【新增订单】参数不正确，form={}", form);
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        OrderRecords result = service.saveOrder(form);
        return ResultVO.success(result);
    }

    @PostMapping("/orderRecords/modifyOrder")
    public ResultVO modifyOrder(@Valid OrderRecordsForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【修改订单】参数不正确，form={}", form);
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        OrderRecords result = service.modifyOrder(form);
        return ResultVO.success(result);
    }

    @GetMapping("/orderRecords/detail")
    public ResultVO detail(@RequestParam(name = "id")Integer id) {
        OrderRecords result = service.detail(id);
        return ResultVO.success(result);
    }

    @GetMapping("/orderRecords/delete")
    public ResultVO delete(@RequestParam(name = "id")Integer id) {
        service.delete(id);
        return ResultVO.success();
    }

    @GetMapping("/orderRecords/exportTemplate")
    public void exportTemplate(HttpServletResponse response) {
        service.exportTemplate(response);
    }

    @PostMapping("/orderRecords/importMateriel")
    public ResultVO importMateriel(MultipartFile file) {
        service.importMateriel(file);
        return ResultVO.success();
    }

    @PostMapping("/orderRecords/putInWare")
    public ResultVO putInWare(@RequestParam(name = "orderId")Integer orderId) {
        List<MaterielLevel> result = service.putInWare(orderId);
        return ResultVO.success(result);
    }

    @PostMapping("/orderRecords/checkOutWare")
    public ResultVO checkOutWare(@RequestParam(name = "orderId")Integer orderId) {
        List<MaterielLevel> result = service.checkOutWare(orderId);
        return ResultVO.success(result);
    }
}
