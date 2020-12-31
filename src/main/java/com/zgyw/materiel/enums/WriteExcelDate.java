package com.zgyw.materiel.enums;

import org.apache.poi.xssf.streaming.SXSSFSheet;

public interface WriteExcelDate {
    /***
     * 向EXCEL写数据/处理格式的类
     * @param eachSheet 第几个sheet
     * @param startRowCount 开始行
     * @param endRowCount 结束行
     * @param currentPage 当前页码
     * @param pageSize 当前页的size
     * @throws Exception
     */
    public abstract void writeExcelData(SXSSFSheet eachSheet, Integer startRowCount, Integer endRowCount,
                                        Integer currentPage, Integer pageSize) ;
}
