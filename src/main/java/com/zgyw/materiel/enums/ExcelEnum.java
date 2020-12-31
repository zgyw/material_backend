package com.zgyw.materiel.enums;

/**
 * @Author:wyt
 * @Date:2020/12/29 18:37
 */
public class ExcelEnum {
    /**
     * 每个sheet存储的记录数 10W
     */
    public static final Integer PER_SHEET_ROW_COUNT = 100000;

    /**
     * 每次向EXCEL写入的记录数(查询每页数据大小) 5000条
     */
    public static final Integer PER_WRITE_ROW_COUNT = 5000;

    /**
     * 每个sheet的写入次数 20
     */
    public static final Integer PER_SHEET_WRITE_COUNT = PER_SHEET_ROW_COUNT / PER_WRITE_ROW_COUNT;
}
