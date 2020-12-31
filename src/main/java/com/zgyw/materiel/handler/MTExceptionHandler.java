package com.zgyw.materiel.handler;


import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.exception.MTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class MTExceptionHandler {

    @ExceptionHandler(value = MTException.class)
    @ResponseBody
    public ResultVO mtExceptionHandler(MTException e){
        log.error(e.getMsg());
        e.printStackTrace();
        return ResultVO.error(e.getCode(),e.getMsg());
    }
}
