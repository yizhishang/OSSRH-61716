package com.yizhishang.common.excel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description Excel导出
 * @Author 袁永君
 * @Date 2018/5/10 11:57
 * @Version 1.0
 **/
@Slf4j
public class ExcelUtils<T> {

    private ThreadLocal<SimpleDateFormat> local = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private int readCount = 0;

    /**
     * 将数据写入到Excel文件
     *
     * @param filePath  文件路径
     * @param sheetName 工作表名称
     * @param title     工作表标题栏
     * @param data      工作表数据
     * @throws FileNotFoundException 文件不存在异常
     * @throws IOException           IO异常
     */
    public static void writeToFile(String filePath, String[] sheetName, List<? extends Object[]> title, List<? extends List<? extends Object[]>> data) throws IOException {
        // 创建并获取工作簿对象
        Workbook wb = getWorkBook(sheetName, title, data);
        if (wb == null) {
            throw new NullPointerException("Workbook对象为空");
        }
        // 写入到文件
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            wb.write(out);
        }
    }

    /**
     * 创建工作簿对象<br>
     * <font color="red">工作表名称，工作表标题，工作表数据最好能够对应起来</font><br>
     * 比如三个不同或相同的工作表名称，三组不同或相同的工作表标题，三组不同或相同的工作表数据<br>
     * <b> 注意：<br>
     * 需要为每个工作表指定<font color="red">工作表名称，工作表标题，工作表数据</font><br>
     * 如果工作表的数目大于工作表数据的集合，那么首先会根据顺序一一创建对应的工作表名称和数据集合，然后创建的工作表里面是没有数据的<br>
     * 如果工作表的数目小于工作表数据的集合，那么多余的数据将不会写入工作表中 </b>
     *
     * @param sheetName 工作表名称的数组
     * @param title     每个工作表名称的数组集合
     * @param data      每个工作表数据的集合的集合
     * @return Workbook工作簿
     * @throws IOException IO异常
     */
    private static Workbook getWorkBook(String[] sheetName, List<? extends Object[]> title, List<? extends List<? extends Object[]>> data) {

        if (CollectionUtils.isEmpty(data)) {
            return null;
        }

        // 创建工作簿
        Workbook wb = new SXSSFWorkbook();
        // 创建一个工作表sheet
        Sheet sheet;
        // 申明行
        Row row;
        // 申明单元格
        Cell cell;
        // 单元格样式
        CellStyle titleStyle = wb.createCellStyle();
        CellStyle cellStyle = wb.createCellStyle();
        // 字体样式
        Font font = wb.createFont();
        // 粗体
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleStyle.setFont(font);
        // 水平居中
        titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
        // 垂直居中
        titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        // 水平居中
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        cellStyle.setFillBackgroundColor(HSSFColor.BLUE.index);

        // 标题数据
        Object[] titleTemp;

        // 行数据
        Object[] rowData;

        // 工作表数据
        List<? extends Object[]> sheetData;

        // 遍历sheet
        for (int sheetNumber = 0; sheetNumber < sheetName.length; sheetNumber++) {
            // 创建工作表
            sheet = wb.createSheet();
            // 设置默认列宽
            sheet.setDefaultColumnWidth(18);
            // 设置工作表名称
            wb.setSheetName(sheetNumber, sheetName[sheetNumber]);
            // 设置标题
            titleTemp = title.get(sheetNumber);
            row = sheet.createRow(0);

            // 写入标题
            for (int i = 0; titleTemp != null && i < titleTemp.length; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(titleStyle);
                cell.setCellValue(titleTemp[i].toString());
            }

            sheetData = data.get(sheetNumber);
            // 写入行数据
            for (int rowNumber = 0; rowNumber < sheetData.size(); rowNumber++) {
                // 如果没有标题栏，起始行就是0，如果有标题栏，行号就应该为1
                row = sheet.createRow(titleTemp == null ? rowNumber : (rowNumber + 1));
                rowData = sheetData.get(rowNumber);
                for (int columnNumber = 0; columnNumber < rowData.length; columnNumber++) {
                    cell = row.createCell(columnNumber);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(rowData[columnNumber] + "");
                }
            }
        }
        return wb;
    }

    /**
     * 将数据写入到EXCEL文档
     *
     * @param list     数据集合
     * @param edf      数据格式化，比如有些数字代表的状态，像是0:女，1：男，或者0：正常，1：锁定，变成可读的文字 该字段仅仅针对Boolean,Integer两种类型作处理
     * @param filePath 文件路径
     * @throws Exception
     */
    public static <T> void writeToFile(List<T> list, ExcelDataFormatter edf, String filePath) throws Exception {
        // 创建并获取工作簿对象
        Workbook wb = getWorkBook(list, edf);

        // 写入到文件
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            wb.write(out);
        }
    }

    /**
     * 将数据写入到EXCEL文档
     *
     * @param list     数据集合
     * @param edf      数据格式化，比如有些数字代表的状态，像是0:女，1：男，或者0：正常，1：锁定，变成可读的文字 该字段仅仅针对Boolean,Integer两种类型作处理
     * @param response 文件路径
     * @throws Exception
     */
    public static <T> void writeToFile(List<T> list, ExcelDataFormatter edf, HttpServletResponse response, String fileName) throws Exception {
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        // 创建并获取工作簿对象
        Workbook wb = getWorkBook(list, edf);
        // 写入到文件
        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();
        out.close();
    }

    /**
     * 获得Workbook对象
     *
     * @param list 数据集合
     * @return Workbook
     * @throws Exception
     */
    public static <T> Workbook getWorkBook(List<T> list, ExcelDataFormatter edf) throws Exception {
        // 创建工作簿
        Workbook wb = new SXSSFWorkbook();

        if (list == null || list.isEmpty()) {
            return wb;
        }

        // 创建一个工作表sheet
        Sheet sheet = wb.createSheet();
        // 申明行
        Row row = sheet.createRow(0);
        // 申明单元格
        Cell cell;

        CreationHelper createHelper = wb.getCreationHelper();

        Field[] fields = list.get(0).getClass().getDeclaredFields();

        XSSFCellStyle titleStyle = (XSSFCellStyle) wb.createCellStyle();

        //titleStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        //设置前景色
        // titleStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(159, 213, 183)));
        titleStyle.setAlignment(CellStyle.ALIGN_CENTER);

        Font font = wb.createFont();
//        font.setColor(HSSFColor.BROWN.index);
//        font.setBoldweight(Font.BOLDWEIGHT_BOLD);

        //设置字体
        titleStyle.setFont(font);

        cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("序号");

        int columnIndex = 1;
        ExcelField excelField;
        for (Field field : fields) {
            field.setAccessible(true);
            excelField = field.getAnnotation(ExcelField.class);
            if (excelField == null || excelField.skip()) {
                continue;
            }
            // 列宽注意乘256
            sheet.setColumnWidth(columnIndex, excelField.width() * 256);
            // 写入标题
            cell = row.createCell(columnIndex);
            cell.setCellStyle(titleStyle);

            String contentName = excelField.name();
            if (StringUtils.isBlank(contentName)) {
                contentName = field.getName();
            }
            cell.setCellValue(contentName);

            columnIndex++;
        }

        int rowIndex = 1;

        CellStyle cs = wb.createCellStyle();

        for (T t : list) {
            row = sheet.createRow(rowIndex);
            columnIndex = 1;

            //创建行号
            cell = row.createCell(0);
            cell.setCellStyle(titleStyle);
            cell.setCellValue(rowIndex);

            Object o;
            for (Field field : fields) {

                field.setAccessible(true);

                // 忽略标记skip的字段
                excelField = field.getAnnotation(ExcelField.class);
                if (excelField == null || excelField.skip()) {
                    continue;
                }
                // 数据
                cell = row.createCell(columnIndex);
                cell.setCellStyle(titleStyle);

                o = field.get(t);
                // 如果数据为空，跳过
                if (o == null) {
                    continue;
                }

                // 处理日期类型
                if (o instanceof Date) {
                    cs.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
                    cell.setCellStyle(cs);
                    cell.setCellValue((Date) field.get(t));
                } else if (o instanceof Double || o instanceof Float) {
                    cell.setCellValue((Double) field.get(t));
                } else if (o instanceof Boolean) {
                    Boolean bool = (Boolean) field.get(t);
                    if (edf == null) {
                        cell.setCellValue(bool);
                    } else {
                        Map<String, String> map = edf.get(field.getName());
                        if (map == null) {
                            cell.setCellValue(bool);
                        } else {
                            cell.setCellValue(map.get(bool.toString().toLowerCase()));
                        }
                    }

                } else if (o instanceof Integer) {

                    Integer intValue = (Integer) field.get(t);

                    if (edf == null) {
                        cell.setCellValue(intValue);
                    } else {
                        Map<String, String> map = edf.get(field.getName());
                        if (map == null) {
                            cell.setCellValue(intValue);
                        } else {
                            cell.setCellValue(map.get(intValue.toString()));
                        }
                    }
                } else {
                    cell.setCellValue(field.get(t).toString());
                }

                columnIndex++;
            }

            rowIndex++;
        }

        return wb;
    }


    /**
     * 从文件读取数据，最好是所有的单元格都是文本格式，日期格式要求yyyy-MM-dd HH:mm:ss,布尔类型0：真，1：假
     *
     * @param edf  数据格式化
     * @param file Excel文件，支持xlsx后缀，xls的没写，基本一样
     * @throws Exception
     */
    public <T> List<T> readFromFile(Class<T> cls, ExcelDataFormatter edf, File file) throws Exception {
        Field[] fields = cls.getDeclaredFields();
        Map<String, String> textToKey = Maps.newHashMap();

        ExcelField excelField;
        for (Field field : fields) {
            excelField = field.getAnnotation(ExcelField.class);
            if (excelField == null || excelField.skip()) {
                continue;
            }
            textToKey.put(excelField.name(), field.getName());
        }

        InputStream is = new FileInputStream(file);

        Workbook wb = new XSSFWorkbook(is);

        Sheet sheet = wb.getSheetAt(0);
        Row title = sheet.getRow(0);
        // 标题数组，后面用到，根据索引去标题名称，通过标题名称去字段名称用到 textToKey
        String[] titles = new String[title.getPhysicalNumberOfCells()];
        for (int i = 0; i < title.getPhysicalNumberOfCells(); i++) {
            titles[i] = title.getCell(i).getStringCellValue();
        }

        List<T> list = Lists.newArrayList();

        int rowIndex = 0;
        int columnCount = titles.length;
        Cell cell;
        Row row;

        for (Iterator<Row> it = sheet.rowIterator(); it.hasNext(); ) {

            row = it.next();
            if (rowIndex++ == 0) {
                continue;
            }

            if (row == null) {
                break;
            }

            T t = cls.newInstance();

            for (int i = 0; i < columnCount; i++) {
                cell = row.getCell(i);
                readCount = 0;
                t = readCellContent(textToKey.get(titles[i]), fields, cell, t, edf);
            }
            list.add(t);
        }
        return list;
    }

    /**
     * 从单元格读取数据，根据不同的数据类型，使用不同的方式读取<br>
     * 有时候POI自作聪明，经常和我们期待的数据格式不一样，会报异常，<br>
     * 我们这里采取强硬的方式<br>
     * 使用各种方法，知道尝试到读到数据为止，然后根据Bean的数据类型，进行相应的转换<br>
     * 如果尝试完了（总共7次），还是不能得到数据，那么抛个异常出来，没办法了
     *
     * @param key    当前单元格对应的Bean字段
     * @param fields Bean所有的字段数组
     * @param cell   单元格对象
     * @throws Exception
     */
    private <T> T readCellContent(String key, Field[] fields, Cell cell, T t, ExcelDataFormatter edf) throws Exception {

        Object o;

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                o = cell.getBooleanCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                o = cell.getNumericCellValue();
                if (DateUtil.isCellDateFormatted(cell)) {
                    o = DateUtil.getJavaDate(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_STRING:
                o = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_ERROR:
                o = cell.getErrorCellValue();
                break;
            case Cell.CELL_TYPE_BLANK:
                o = null;
                break;
            case Cell.CELL_TYPE_FORMULA:
                o = cell.getCellFormula();
                break;
            default:
                o = null;
                break;
        }

        if (o == null) {
            return t;
        }

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getName().equals(key)) {
                    boolean bool = true;
                    Map<String, String> map = null;
                    if (edf == null) {
                        bool = false;
                    } else {
                        map = edf.get(field.getName());
                        if (map == null) {
                            bool = false;
                        }
                    }

                    if (field.getType().equals(Date.class)) {
                        if (o.getClass().equals(Date.class)) {
                            field.set(t, o);
                        } else {
                            field.set(t, local.get().parse(o.toString()));
                        }
                    } else if (field.getType().equals(String.class)) {
                        if (o.getClass().equals(String.class)) {
                            field.set(t, o);
                        } else {
                            field.set(t, o.toString());
                        }
                    } else if (field.getType().equals(Long.class)) {
                        if (o.getClass().equals(Long.class)) {
                            field.set(t, o);
                        } else {
                            field.set(t, Long.parseLong(o.toString()));
                        }
                    } else if (field.getType().equals(Integer.class)) {
                        if (o.getClass().equals(Integer.class)) {
                            field.set(t, o);
                        } else {
                            // 检查是否需要转换
                            if (bool) {
                                field.set(t, map.get(o.toString()) != null ? Integer.parseInt(map.get(o.toString())) : Integer.parseInt(o.toString()));
                            } else {
                                field.set(t, Integer.parseInt(o.toString()));
                            }

                        }
                    } else if (field.getType().equals(BigDecimal.class)) {
                        if (o.getClass().equals(BigDecimal.class)) {
                            field.set(t, o);
                        } else {
                            field.set(t, BigDecimal.valueOf(Double.parseDouble(o.toString())));
                        }
                    } else if (field.getType().equals(Boolean.class)) {
                        if (o.getClass().equals(Boolean.class)) {
                            field.set(t, o);
                        } else {
                            // 检查是否需要转换
                            if (bool) {
                                field.set(t, map.get(o.toString()) != null ? Boolean.parseBoolean(map.get(o.toString())) : Boolean.parseBoolean(o.toString()));
                            } else {
                                field.set(t, Boolean.parseBoolean(o.toString()));
                            }
                        }
                    } else if (field.getType().equals(Float.class)) {
                        if (o.getClass().equals(Float.class)) {
                            field.set(t, o);
                        } else {
                            field.set(t, Float.parseFloat(o.toString()));
                        }
                    } else if (field.getType().equals(Double.class)) {
                        if (o.getClass().equals(Double.class)) {
                            field.set(t, o);
                        } else {
                            field.set(t, Double.parseDouble(o.toString()));
                        }

                    }

                }
            }

        } catch (Exception e) {
            log.error("readCellContent方法报错", e);
            // 如果还是读到的数据格式还是不对，只能放弃了
            if (readCount > 7) {
                throw e;
            }
            readCount++;
            t = readCellContent(key, fields, cell, t, edf);
        }
        return t;
    }

}
