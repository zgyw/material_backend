package com.zgyw.materiel.controller;


import com.zgyw.materiel.VO.MaterielRecordsVO;
import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.service.MaterielRecordsService;
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
                                  @RequestParam(name = "content",defaultValue = "") String content) {
        List<MaterielRecordsVO> result = service.findCurOrder(orderId,content);
        return ResultVO.success(result);
    }

    @GetMapping("/materielRecords/detail")
    public ResultVO detail(@RequestParam(name = "id")Integer id) {
        MaterielRecordsVO result = service.detail(id);
        return ResultVO.success(result);
    }

    @GetMapping("/materielRecords/delete")
    public ResultVO delete(@RequestParam(name = "id")Integer id) {
        service.delete(id);
        return ResultVO.success();
    }

    @PostMapping("/materielRecords/modify")
    public ResultVO modify(@Valid MaterielRecords records, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【修改订单明细】参数不正确，records={}", records);
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        MaterielRecords result = service.modify(records);
        return ResultVO.success(result);
    }

    @PostMapping("/materielRecords/changeInNum")
    public ResultVO changeInNum(@RequestParam(name = "materielNums") String materielNums) {
        List<MaterielRecords> result = service.changeInNum(materielNums);
        return ResultVO.success(result);
    }

    @PostMapping("/materielRecords/changeOutNum")
    public ResultVO changeOutNum(@RequestParam(name = "materielNums") String materielNums) {
        List<MaterielRecords> result = service.changeOutNum(materielNums);
        return ResultVO.success(result);
    }


}
