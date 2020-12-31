package com.zgyw.materiel.enums;

import lombok.Getter;

@Getter
public enum  ResultEnum implements CodeEnum {
    SUCCESS(0,"成功"),
    ERROR(1,"失败！"),
    EMPTY_USER(2,"用户不存在"),
    USER_ERROR(3,"密码错误"),
    PARAM_ERROR(4,"参数错误"),
    CODE_EXIST(5,"编码已存在"),
    EXCEL_ERROR(6,"解析文件格式有误!"),
    FILE_EMPTY(7,"导入文件为空!"),
    FILE_ERROR(8,"请使用正确模板!"),
    FAIL(500,"服务器开小差!")
    ;

    private final Integer code;

    private final String message;

    ResultEnum(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}
