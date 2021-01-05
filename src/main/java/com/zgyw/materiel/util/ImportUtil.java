package com.zgyw.materiel.util;

import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:wyt
 * @Date:2020/12/29 18:39
 */
@Slf4j
public class ImportUtil {
    /**
     * 将excel 的数据变成list集合
     *
     * @param workbook
     * @return
     */
    public static List<List<Object>> getBankListByExcel(Workbook workbook) {
        List<List<Object>> list = null;
        //创建Excel工作簿
        try {
            Sheet sheet = null;
            Row row = null;
            Cell cell = null;
            list = new ArrayList<List<Object>>();
            //遍历Excel中的所有sheet
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheet = workbook.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }
                //遍历当前sheet中的所有行
                int totalRow = sheet.getPhysicalNumberOfRows();
                //sheet.getFirstRowNum()
                for (int j = sheet.getFirstRowNum() + 1; j < totalRow; j++) {
                    row = sheet.getRow(j);
                    if (!isRowEmpty(row)) {
                        //获取第一个单元格的数据是否存在
                        Cell fristCell = row.getCell(0);
                        if (fristCell != null) {
                            //遍历所有的列
                            List<Object> innerList = new ArrayList<Object>();
                            for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                                cell = row.getCell(y);
                                String callCal = getCellValue(cell) + "";
                                innerList.add(callCal);
                            }
                            list.add(innerList);
                        } else {
                            //遍历所有的列
                            List<Object> innerList = new ArrayList<Object>();
                            for (int y = 0; y < row.getLastCellNum(); y++) {
                                cell = row.getCell(y);
                                String callCal = getCellValue(cell) + "";
                                innerList.add(callCal);
                            }
                            list.add(innerList);

                        }
                    } else if (isRowEmpty(row)) {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MTException(ResultEnum.FAIL);
        }
        return list;
    }

    /**
     * 判断行是否为空
     *
     * @param row
     * @return
     */
    public static boolean isRowEmpty(Row row) {
        //计算单元格的起始位置和结束位置
        if (row != null) {
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                    return false;
            }
        }
        return true;
    }

    /**
     * 解决获取一次workbook操作结束流关闭的问题
     *
     * @param inputStream 文件输入流
     * @return
     */
    public static Workbook getWorkbook(InputStream inputStream) {
        Workbook work = null;
        try {
            if (!inputStream.markSupported()) {
                inputStream = new PushbackInputStream(inputStream, 8);
            }
            if (POIFSFileSystem.hasPOIFSHeader(inputStream)) {
                work = new HSSFWorkbook(inputStream);
            } else if (POIXMLDocument.hasOOXMLHeader(inputStream)) {
                work = new XSSFWorkbook(inputStream);
            }
        } catch (Exception e) {
            log.error("excel操作异常", e);
            throw new MTException(ResultEnum.FAIL);
        }
        return work;
    }

    /**
     * 格式化单元格的数据
     *
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        String result = "";
        if (cell != null) {
            //判断单元格的格式，再格式化
            switch (cell.getCellType()) {
                // String类型
                case HSSFCell.CELL_TYPE_STRING:
                    result = cell.getRichStringCellValue().toString();
                    break;
                // 数字类型
                case HSSFCell.CELL_TYPE_NUMERIC:
                    DecimalFormat format = new DecimalFormat("#.#####");
                    double value = cell.getNumericCellValue();
                    if (String.valueOf(value).contains("e") || String.valueOf(value).contains("E")) {
                        BigDecimal bigDecimal = new BigDecimal(String.valueOf(value));
                        result = String.valueOf(bigDecimal.toPlainString());
                    } else {
                        result = format.format(value);
                    }
                    break;
                default:
                    result = "";
                    break;
            }
        }
        return result;
    }

    /** 获取到导入文件的头行 */
    public static List<String> getFirstRowContent (MultipartFile file) {
        List<String> firstRowList = new ArrayList<>();
        try {
            Workbook workbook = ImportUtil.getWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(sheet.getFirstRowNum());
            Cell cell = null;
            if (!isRowEmpty(row)) {
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    String callCal = getCellValue(cell) + "";
                    firstRowList.add(callCal);
                }
            }
        } catch (IOException e) {
            log.error("excel操作异常", e);
            throw new MTException(ResultEnum.FAIL);
        }
        return firstRowList;
    }

    /**
     * 检测文件是否合法
     * @param file 文件
     * @param content 第一个单元格内容
     * @param colNum 文件列数
     */
    public static List<List<Object>> checkFile (MultipartFile file,String content,Integer colNum) {
        List<List<Object>> dataList = ExportUtil.read(file);
        List<String> firstRow = ImportUtil.getFirstRowContent(file);
        if (dataList.size() == 0) {
            throw new MTException(ResultEnum.FILE_EMPTY);
        }
        if (firstRow.size() == 0 || !content.equals(firstRow.get(0)) || firstRow.size() != colNum) {
            throw new MTException(ResultEnum.FILE_ERROR);
        }
        return dataList;
    }
}
