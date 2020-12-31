package com.zgyw.materiel.exception;


import com.zgyw.materiel.enums.ResultEnum;
import lombok.Data;

@Data
public class MTException extends RuntimeException{

    private static final long serialVersionUID = 2787462943129202183L;

    private String msg;
    private int code;

    public MTException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public MTException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.msg = resultEnum.getMessage();
        this.code = resultEnum.getCode();
    }

    public MTException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public MTException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public MTException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
}
