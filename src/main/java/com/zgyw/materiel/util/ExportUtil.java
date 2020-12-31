package com.zgyw.materiel.util;

import com.zgyw.materiel.enums.ExcelEnum;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.enums.WriteExcelDate;
import com.zgyw.materiel.exception.MTException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author:wyt
 * @Date:2020/12/29 18:38
 */
@Slf4j
public class ExportUtil {

    private static SXSSFWorkbook workbook = null;

    private static XSSFWorkbook xssfWorkbook = null;

    private static SXSSFSheet sheet = null;

    /***
     *
     * @param totalRowCount 总行数
     * @return
     */
    public static SXSSFWorkbook getWorkbook(int totalRowCount) {
        try {
            workbook = new SXSSFWorkbook(6000);
            //一个sheet可以创建100000行，数据量超出时就新建一个sheet
            Integer sheetCount = ((totalRowCount % ExcelEnum.PER_SHEET_ROW_COUNT == 0) ?
                    (totalRowCount / ExcelEnum.PER_SHEET_ROW_COUNT) : (totalRowCount / ExcelEnum.PER_SHEET_ROW_COUNT + 1));
            //如果sheetCount 大于1 还要创建sheet进行存储数据
            for (int i = 0; i < sheetCount; i++) {
                sheet = workbook.createSheet("Sheet" + (i + 1));
            }
        } catch (Exception e) {
            log.error("excel操作异常", e);
            throw new MTException(ResultEnum.FAIL);
        }
        return workbook;
    }

    /***
     *
     * @param totalRowCount 总行数
     * @param is 文件流
     * @return
     */
    public static SXSSFWorkbook getWorkbook(int totalRowCount, InputStream is) {
        try {
            //先获取模板的文件
            xssfWorkbook = new XSSFWorkbook(is);
            //通过模板创建Excel
            workbook = new SXSSFWorkbook(xssfWorkbook, -1);
            //一个sheet可以创建100000行，数据量超出时就新建一个sheet
            Integer sheetCount = ((totalRowCount % ExcelEnum.PER_SHEET_ROW_COUNT == 0) ?
                    (totalRowCount / ExcelEnum.PER_SHEET_ROW_COUNT) : (totalRowCount / ExcelEnum.PER_SHEET_ROW_COUNT + 1));
            //得到模板中的sheet
            sheet = workbook.getSheetAt(0);
            //如果sheetCount 大于1 还要创建sheet进行存储数据
            for (int i = 1; i < sheetCount; i++) {
                SXSSFSheet sheet2 = workbook.createSheet("sheet" + (i + 1));
            }
        } catch (IOException e) {
            log.error("excel操作异常", e);
            throw new MTException(ResultEnum.FAIL);
        }
        return workbook;
    }

    /**
     * 下载EXCEL到浏览器
     *
     * @param wb
     * @param response
     * @param fileName 文件名称
     * @throws IOException
     */
    public static void downLoadExcelToWebsite(SXSSFWorkbook wb, HttpServletResponse response, String fileName) {

        OutputStream outputStream = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.addHeader("Content-disposition", "attachment; filename="
                    + new String((fileName + ".xlsx").getBytes(), "ISO8859-1"));//设置下载的文件名
            outputStream = response.getOutputStream();
            wb.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            log.error("excel操作异常", e);
            throw new MTException(ResultEnum.FAIL);
        } finally {
            if (wb != null) {
                try {
                    wb.dispose();
                } catch (Exception e) {
                    log.error("excel操作异常", e);
                    throw new MTException(ResultEnum.FAIL);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.error("excel操作异常", e);
                    throw new MTException(ResultEnum.FAIL);
                }
            }
        }
    }

    /**
     * 导出Excel到浏览器
     *
     * @param response
     * @param totalRowCount  总记录数
     * @param fileName       文件名称
     * @param is             文件流
     * @param writeExcelData 向EXCEL写数据/处理格式的委托类 自行实现
     * @throws Exception
     */
    public static final void exportExcelToWebsite(HttpServletResponse response, Integer totalRowCount,
                                                  InputStream is, String fileName, WriteExcelDate writeExcelData) {

        log.info("开始导出：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        long begin = System.currentTimeMillis();
        // 初始化EXCEL
        SXSSFWorkbook workbook = getWorkbook(totalRowCount, is);
        // 调用处理数据的类分批写数据
        int sheetCount = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            SXSSFSheet eachSheet = workbook.getSheetAt(i);
            for (int j = 1; j <= ExcelEnum.PER_SHEET_WRITE_COUNT; j++) {
                int currentPage = i * ExcelEnum.PER_SHEET_WRITE_COUNT + j;
                int pageSize = ExcelEnum.PER_WRITE_ROW_COUNT;
                int startRowCount = (j - 1) * ExcelEnum.PER_WRITE_ROW_COUNT + 1;
                int endRowCount = startRowCount + pageSize - 1;
                writeExcelData.writeExcelData(eachSheet, startRowCount, endRowCount, currentPage, pageSize);
                if (totalRowCount < ExcelEnum.PER_WRITE_ROW_COUNT) {
                    break;
                }
            }
        }
        // 下载EXCEL
        downLoadExcelToWebsite(workbook, response, fileName);
        log.info("导出完成：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        long end = System.currentTimeMillis();
        log.info("耗时：" + (end - begin));
    }

    /**
     * 导出自定义Excel到浏览器
     *
     * @param response
     * @param totalRowCount  总记录数
     * @param fileName       文件名称
     * @param writeExcelData 向EXCEL写数据/处理格式的委托类 自行实现
     * @throws Exception
     */
    public static final void exportExcelToWebsite(HttpServletResponse response, Integer totalRowCount, String fileName,String[] headers, WriteExcelDate writeExcelData) {

        log.info("开始导出：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        long begin = System.currentTimeMillis();
        // 初始化EXCEL
        SXSSFWorkbook workbook = getWorkbook(totalRowCount);
        // 调用处理数据的类分批写数据
        int sheetCount = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            SXSSFSheet sheet = workbook.getSheetAt(i);
            /** 创建头行 */
            SXSSFRow row = sheet.createRow(0);
            for (int i1 = 0; i1 < headers.length; i1++) {
                row.createCell(i1).setCellValue(headers[i1]);
            }
            sheet.createFreezePane(0,1,0,1);
            for (int j = 0; j < (totalRowCount/ExcelEnum.PER_WRITE_ROW_COUNT)+1; j++) {
                /** 查询的每页条数 */
                int pageSize = ExcelEnum.PER_WRITE_ROW_COUNT;
                int startRowCount = j * ExcelEnum.PER_WRITE_ROW_COUNT + 1;
                int endRowCount = startRowCount + pageSize - 1;
                /** 查询的页码*/
                int currentPage = j;
                writeExcelData.writeExcelData(sheet, startRowCount, endRowCount, currentPage, pageSize);
            }
        }
        // 下载EXCEL
        downLoadExcelToWebsite(workbook, response, fileName);
        log.info("导出完成：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        long end = System.currentTimeMillis();
        log.info("耗时：" + (end - begin));
    }

    /**
     * 判断excel的格式
     *
     * @param file 文件流
     * @return
     */
    public static InputStream readExcel(MultipartFile file) {
        //判断当前文件路径是否存在，或者是否符合excel表格
        String fileName = file.getOriginalFilename();
        InputStream inputStream = null;
        if (fileName != null || fileName.matches("^.+\\.(?i)((xls)|(xlsx))$")) {
            inputStream = getImportExcel(file);
        } else {
            log.info("文件不存在或者文件格式不正确：导入失败！，文件名:{}", fileName);
        }
        return inputStream;
    }

    /**
     * 判断excel的格式
     *
     * @param file 文件流
     * @return
     */
    public static List<List<Object>> read(MultipartFile file) {
        //判断当前文件路径是否存在，或者是否符合excel表格
        String fileName = file.getOriginalFilename();
        List<List<Object>> dataList = new ArrayList<>();
        if (fileName != null || fileName.matches("^.+\\.(?i)((xls)|(xlsx))$")) {
            try {
                Workbook workbook = ImportUtil.getWorkbook(file.getInputStream());
                dataList = ImportUtil.getBankListByExcel(workbook);
            } catch (Exception e) {
                log.error("excel操作异常", e);
                throw new MTException(ResultEnum.FAIL);
            }
        } else {
            log.info("文件不存在或者文件格式不正确：导入失败！，文件名:{}", fileName);
        }
        return dataList;
    }

    /**
     * @param file
     * @return
     */
    public static InputStream getImportExcel(MultipartFile file) {
        InputStream inputStream = null;
        try {
            String fileName = file.getOriginalFilename();
            inputStream = file.getInputStream();
        } catch (Exception e) {
            log.error("excel操作异常", e);
            throw new MTException(ResultEnum.FAIL);
        }
        return inputStream;
    }
}
