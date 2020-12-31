package com.zgyw.materiel.controller;


import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.service.MaterielRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MaterielRecordsController {

    @Autowired
    private MaterielRecordsService service;

    @PostMapping("/materielRecords/putInOrder")
    public ResultVO putInOrder(@RequestParam(name = "orderId") Integer orderId,
                               @RequestParam(name = "materielIds") String materielIds,
                               @RequestParam(name = "type") Integer type) {
        List<MaterielRecords> result = service.putInOrder(orderId, materielIds,type);
        return ResultVO.success(result);
    }

    @GetMapping("/materielRecords/findCurOrder")
    public ResultVO findCurOrder (@RequestParam(name = "orderId") Integer orderId) {
        List<MaterielRecords> result = service.findCurOrder(orderId);
        return ResultVO.success(result);
    }
}
