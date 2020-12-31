package com.zgyw.materiel.controller;


import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.OrderRecords;
import com.zgyw.materiel.service.OrderRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
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
        PageRequest pageRequest = PageRequest.of(page, size);
        Map<String, Object> result = service.pageList(type, status, content, pageRequest);
        return ResultVO.success(result);
    }

    @PostMapping("/orderRecords/saveOrder")
    public ResultVO saveOrder(@RequestParam(name = "name")String name,
                              @RequestParam(name = "remarks")String remarks,
                              @RequestParam(name = "type")Integer type) {
        OrderRecords result = service.saveOrder(name, remarks, type);
        return ResultVO.success(result);
    }
}
