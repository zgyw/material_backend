package com.zgyw.materiel.controller;


import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.service.MaterielRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    public ResultVO findCurOrder (@RequestParam(name = "orderId") Integer orderId,
                                  @RequestParam(name = "content",defaultValue = "") String content,
                                  @RequestParam(name = "page",defaultValue = "0")Integer page,
                                  @RequestParam(name = "size",defaultValue = "10")Integer size) {
        PageRequest pageRequest = PageRequest.of(page,size);
        Map<String,Object> result = service.findCurOrder(orderId,content,pageRequest);
        return ResultVO.success(result);
    }
}
