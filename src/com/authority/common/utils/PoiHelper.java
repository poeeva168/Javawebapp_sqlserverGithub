package com.authority.common.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoiHelper {
	private static final Logger logger = LoggerFactory
			.getLogger(PoiHelper.class);

	/**
	 * 合并单元格样式
	 * @param sheet
	 * @param region
	 * @param cs
	 */
	/*public void setRegionStyle(HSSFSheet sheet, Region region, HSSFCellStyle cs) {
		int toprowNum = region.getRowFrom();
		for (int i = region.getRowFrom(); i <= region.getRowTo(); i++) {
			HSSFRow row = HSSFCellUtil.getRow(i, sheet);
			for (int j = region.getColumnFrom(); j <= region.getColumnTo(); j++) {
				HSSFCell cell = HSSFCellUtil.getCell(row, (short) j);
				cell.setCellStyle(cs);
			}

		}

	}*/
	
	/**
	 * 某一目录下生成Excel 文件
	 * @param list
	 * @param col_id
	 * @param col_name
	 * @param filePath
	 */
	public static void Excel_Generate(List<Map<String, Object>> list,
			String[] col_id, String[] col_name, String filePath,Boolean Excel2007){
		try {
			
			File saveFile = new File(filePath);
			saveFile.delete();
			if(!saveFile.getParentFile().exists())
				saveFile.getParentFile().mkdirs();
			saveFile.createNewFile();
			
			FileOutputStream out = new FileOutputStream(saveFile);
			
			if(Excel2007){
				//生成Excel
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Sheet sheet = wb.createSheet("sheet"); 
				/*Row row = sheet.createRow(0);
				Cell cell = row.createCell(0);
				cell.setCellStyle(arg0)*/
				
				// 普通样式
				CellStyle cellStyle = wb.createCellStyle();
				cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
				cellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				// 设置边框
				cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				// 自动换行
				cellStyle.setWrapText(false);
				// 字体设置
				Font font = wb.createFont();
				font.setFontHeightInPoints((short) 9);
				font.setFontName("宋体");
				// 打印设置
				PrintSetup printSetup = sheet.getPrintSetup();
				printSetup.setLandscape(false); // 打印方向，true:横向，false:纵向
				printSetup.setPaperSize(XSSFPrintSetup.A4_PAPERSIZE); // 纸张
				sheet.setMargin(Sheet.BottomMargin, (double) 0.3); // 页边距（下）
				sheet.setMargin(Sheet.LeftMargin, (double) 0.3); // 页边距（左）
				sheet.setMargin(Sheet.RightMargin, (double) 0.3); // 页边距（右）
				sheet.setMargin(Sheet.TopMargin, (double) 0.3); // 页边距（上）
				sheet.setHorizontallyCenter(true); // 设置打印页面为水平居中

				cellStyle.setFont(font);

				if (1 > 0) {
					Row row = sheet.createRow(0);
					// 标题区
					for (int i = 0; i < col_name.length; i++) {
						Cell cell = row.createCell(i);
						cell.setCellType(XSSFCell.CELL_TYPE_STRING);
						cell.setCellValue(col_name[i]);
						cell.setCellStyle(cellStyle);
					}
				}
				// 数据区
				int num = 1; // 序号
				int row_num = 1;
				for (Map<String, Object> map : list) {
					Row row = sheet.createRow(row_num);
					for (int i = 0; i < col_id.length; i++) {
						Cell cell = row.createCell(i);
						if (col_id[i].equals("NUM")){
							cell.setCellType(XSSFCell.CELL_TYPE_STRING);
							cell.setCellValue(String.valueOf(num));
						}else {
							cell.setCellType(XSSFCell.CELL_TYPE_STRING);
							cell.setCellValue(StringprocessHelper.String_html(String
									.valueOf(map.get(col_id[i]
											.toLowerCase()))));
						}
						cell.setCellStyle(cellStyle);
					}
					num++;
					row_num++;
				}
				
				wb.write(out);
				out.flush();
				out.close();
								
			}else{
				// 生成Excel
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = wb.createSheet("sheet");
				// 普通样式
				HSSFCellStyle Style_default = wb.createCellStyle();
				Style_default.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				Style_default.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				// 设置边框
				Style_default.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				Style_default.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				Style_default.setBorderRight(HSSFCellStyle.BORDER_THIN);
				Style_default.setBorderTop(HSSFCellStyle.BORDER_THIN);
				// 自动换行
				Style_default.setWrapText(false);
				// 字体设置
				HSSFFont font_default = wb.createFont();
				font_default.setFontHeightInPoints((short) 9);
				font_default.setFontName("宋体");
				// 打印设置
				HSSFPrintSetup ps = sheet.getPrintSetup();
				ps.setLandscape(false); // 打印方向，true:横向，false:纵向
				ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); // 纸张
				sheet.setMargin(HSSFSheet.BottomMargin, (double) 0.3); // 页边距（下）
				sheet.setMargin(HSSFSheet.LeftMargin, (double) 0.3); // 页边距（左）
				sheet.setMargin(HSSFSheet.RightMargin, (double) 0.3); // 页边距（右）
				sheet.setMargin(HSSFSheet.TopMargin, (double) 0.3); // 页边距（上）
				sheet.setHorizontallyCenter(true); // 设置打印页面为水平居中

				Style_default.setFont(font_default);

				if (1 > 0) {
					HSSFRow row = sheet.createRow(0);
					// 标题区
					for (int i = 0; i < col_name.length; i++) {
						HSSFCell cell = row.createCell(i);
						cell.setCellValue(new HSSFRichTextString(col_name[i]));
						cell.setCellStyle(Style_default);
					}
				}
				// 数据区
				int num = 1; // 序号
				int row_num = 1;

				for (Map<String, Object> map : list) {

					HSSFRow row = sheet.createRow(row_num);

					for (int i = 0; i < col_id.length; i++) {

						HSSFCell cell = row.createCell(i);

						if (col_id[i].equals("NUM"))
							cell.setCellValue(new HSSFRichTextString(String
									.valueOf(num)));
						else {

							cell.setCellValue(new HSSFRichTextString(
									StringprocessHelper.String_html(String
											.valueOf(map.get(col_id[i]
													.toLowerCase())))));
						}
						cell.setCellStyle(Style_default);
					}
					num++;
					row_num++;
				}
				
				wb.write(out);
				out.flush();
				out.close();				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 生成Excel 下载
	 * @param list
	 * @param col_id
	 * @param col_name
	 * @param fileName 
	 */

	public static void Excel_pro(List<Map<String, Object>> list,
			String[] col_id, String[] col_name, String fileName ,Boolean Excel2007) {
		try {
			
			HttpServletResponse response = null;

			response.setHeader("Content-disposition", "attachment; filename="
					+ new String(fileName.getBytes("GBK"), "ISO8859_1"));// 下载方式，下载，不用浏览器打开
			response.setContentType("application/vnd.ms-excel");

			ServletOutputStream out = response.getOutputStream();
			
			if(Excel2007){
				//生成Excel
				SXSSFWorkbook wb = new SXSSFWorkbook(100);
				Sheet sheet = wb.createSheet("sheet"); 
				/*Row row = sheet.createRow(0);
				Cell cell = row.createCell(0);
				cell.setCellStyle(arg0)*/
				
				// 普通样式
				CellStyle cellStyle = wb.createCellStyle();
				cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
				cellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				// 设置边框
				cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				// 自动换行
				cellStyle.setWrapText(false);
				// 字体设置
				Font font = wb.createFont();
				font.setFontHeightInPoints((short) 9);
				font.setFontName("宋体");
				// 打印设置
				PrintSetup printSetup = sheet.getPrintSetup();
				printSetup.setLandscape(false); // 打印方向，true:横向，false:纵向
				printSetup.setPaperSize(XSSFPrintSetup.A4_PAPERSIZE); // 纸张
				sheet.setMargin(Sheet.BottomMargin, (double) 0.3); // 页边距（下）
				sheet.setMargin(Sheet.LeftMargin, (double) 0.3); // 页边距（左）
				sheet.setMargin(Sheet.RightMargin, (double) 0.3); // 页边距（右）
				sheet.setMargin(Sheet.TopMargin, (double) 0.3); // 页边距（上）
				sheet.setHorizontallyCenter(true); // 设置打印页面为水平居中

				cellStyle.setFont(font);

				if (1 > 0) {
					Row row = sheet.createRow(0);
					// 标题区
					for (int i = 0; i < col_name.length; i++) {
						Cell cell = row.createCell(i);
						cell.setCellType(XSSFCell.CELL_TYPE_STRING);
						cell.setCellValue(col_name[i]);
						cell.setCellStyle(cellStyle);
					}
				}
				// 数据区
				int num = 1; // 序号
				int row_num = 1;
				for (Map<String, Object> map : list) {
					Row row = sheet.createRow(row_num);
					for (int i = 0; i < col_id.length; i++) {
						Cell cell = row.createCell(i);
						if (col_id[i].equals("NUM")){
							cell.setCellType(XSSFCell.CELL_TYPE_STRING);
							cell.setCellValue(String.valueOf(num));
						}else {
							cell.setCellType(XSSFCell.CELL_TYPE_STRING);
							cell.setCellValue(StringprocessHelper.String_html(String
									.valueOf(map.get(col_id[i]
											.toLowerCase()))));
						}
						cell.setCellStyle(cellStyle);
					}
					num++;
					row_num++;
				}
				
				wb.write(out);
				out.flush();
				out.close();
								
			}else{
				// 生成Excel
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = wb.createSheet("sheet");
				// 普通样式
				HSSFCellStyle Style_default = wb.createCellStyle();
				Style_default.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				Style_default.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				// 设置边框
				Style_default.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				Style_default.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				Style_default.setBorderRight(HSSFCellStyle.BORDER_THIN);
				Style_default.setBorderTop(HSSFCellStyle.BORDER_THIN);
				// 自动换行
				Style_default.setWrapText(false);
				// 字体设置
				HSSFFont font_default = wb.createFont();
				font_default.setFontHeightInPoints((short) 9);
				font_default.setFontName("宋体");
				// 打印设置
				HSSFPrintSetup ps = sheet.getPrintSetup();
				ps.setLandscape(false); // 打印方向，true:横向，false:纵向
				ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); // 纸张
				sheet.setMargin(HSSFSheet.BottomMargin, (double) 0.3); // 页边距（下）
				sheet.setMargin(HSSFSheet.LeftMargin, (double) 0.3); // 页边距（左）
				sheet.setMargin(HSSFSheet.RightMargin, (double) 0.3); // 页边距（右）
				sheet.setMargin(HSSFSheet.TopMargin, (double) 0.3); // 页边距（上）
				sheet.setHorizontallyCenter(true); // 设置打印页面为水平居中

				Style_default.setFont(font_default);

				if (1 > 0) {
					HSSFRow row = sheet.createRow(0);
					// 标题区
					for (int i = 0; i < col_name.length; i++) {
						HSSFCell cell = row.createCell(i);
						cell.setCellValue(new HSSFRichTextString(col_name[i]));
						cell.setCellStyle(Style_default);
					}
				}
				// 数据区
				int num = 1; // 序号
				int row_num = 1;

				for (Map<String, Object> map : list) {

					HSSFRow row = sheet.createRow(row_num);

					for (int i = 0; i < col_id.length; i++) {

						HSSFCell cell = row.createCell(i);

						if (col_id[i].equals("NUM"))
							cell.setCellValue(new HSSFRichTextString(String
									.valueOf(num)));
						else {

							cell.setCellValue(new HSSFRichTextString(
									StringprocessHelper.String_html(String
											.valueOf(map.get(col_id[i]
													.toLowerCase())))));
						}
						cell.setCellStyle(Style_default);
					}
					num++;
					row_num++;
				}
				
				wb.write(out);
				out.flush();
				out.close();				
			}

		} catch (Exception e) {
			logger.error("Exception: ", e);
		}

	}
	
	/**
     * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是多少个行
     * @param file 读取数据的源Excel
     * @param ignoreRows 读取数据忽略的行数，比如行头不需要读入 忽略的行数为1
     * @return 读出的Excel中数据的内容
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String[]> getData(File file, int ignoreRows,int sheetIndex)
           throws FileNotFoundException, IOException {
       List<String[]> result = new ArrayList<String[]>();
       int rowSize = 0;
       BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
       //获取文件后缀
       String fileType = StringUtils.substringAfterLast(file.getName(), ".");
       Boolean Excel2007 = true;
       if(fileType.equalsIgnoreCase("xls"))
    	   Excel2007 = false;
       
       if(Excel2007){
    	   
    	// 打开HSSFWorkbook  第一种
    	   XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream(in)); 
           
           Cell cell = null;
           
           Sheet sheet = wb.getSheetAt(sheetIndex);
           // 第一行为标题，不取
           for (int rowIndex = ignoreRows; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
              Row row = sheet.getRow(rowIndex);
              if (row == null) {
                  continue;
              }
              int tempRowSize = row.getLastCellNum() + 1;
              if (tempRowSize > rowSize) {
                  rowSize = tempRowSize;
              }
              String[] values = new String[rowSize];
              Arrays.fill(values, "");
              boolean hasValue = false;
              for (int columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
                  String value = "";
                  cell = row.getCell(columnIndex);
                  if (cell != null) {
                     // 注意：一定要设成这个，否则可能会出现乱码                	  
              //       cell.setEncoding(HSSFCell.ENCODING_UTF_16);                     
                     switch (cell.getCellType()) {
                     case XSSFCell.CELL_TYPE_STRING:
                         value = cell.getStringCellValue();
                         break;
                     case XSSFCell.CELL_TYPE_NUMERIC:
                         if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            Date date = cell.getDateCellValue();
                            if (date != null) {
                                value = new SimpleDateFormat("yyyy-MM-dd")
                                       .format(date);
                            } else {
                                value = "";
                            }
                         } else {
                            value = new DecimalFormat("0").format(cell
                                   .getNumericCellValue());
                         }
                         break;
                     case XSSFCell.CELL_TYPE_FORMULA:
                         // 导入时如果为公式生成的数据则无值
                    	 try{
                    		 value = String.valueOf(cell.getNumericCellValue());
                    	 }catch(IllegalStateException e){
                    		 value = String.valueOf(cell.getRichStringCellValue());
                    	 }                	 
                         /*if (!cell.getStringCellValue().equals("")) {
                            value = cell.getStringCellValue();
                         } else {
                            value = cell.getNumericCellValue() + "";
                         }*/
                         break;
                     case XSSFCell.CELL_TYPE_BLANK:
                         break;
                     case XSSFCell.CELL_TYPE_ERROR:
                         value = "";
                         break;
                     case XSSFCell.CELL_TYPE_BOOLEAN:
                         value = (cell.getBooleanCellValue() == true ? "Y"
                                : "N");
                         break;
                     default:
                         value = "";
                     }
                  }
                  if (columnIndex == 0 && value.trim().equals("")) {
                         break;
                      }
                  values[columnIndex] = rightTrim(value);
                  hasValue = true;
              }
 
              if (hasValue) {
                  result.add(values);
              }
              
           }

           in.close();
           
       }else{
    	   
    	// 打开HSSFWorkbook  第一种
           POIFSFileSystem fs = new POIFSFileSystem(in);
           HSSFWorkbook wb = new HSSFWorkbook(fs);
           
           HSSFCell cell = null;
           
           HSSFSheet st = wb.getSheetAt(sheetIndex);
           // 第一行为标题，不取
           for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
              HSSFRow row = st.getRow(rowIndex);
              if (row == null) {
                  continue;
              }
              int tempRowSize = row.getLastCellNum() + 1;
              if (tempRowSize > rowSize) {
                  rowSize = tempRowSize;
              }
              String[] values = new String[rowSize];
              Arrays.fill(values, "");
              boolean hasValue = false;
              for (int columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
                  String value = "";
                  cell = row.getCell(columnIndex);
                  if (cell != null) {
                     // 注意：一定要设成这个，否则可能会出现乱码                	  
              //       cell.setEncoding(HSSFCell.ENCODING_UTF_16);                     
                     switch (cell.getCellType()) {
                     case HSSFCell.CELL_TYPE_STRING:
                         value = cell.getStringCellValue();
                         break;
                     case HSSFCell.CELL_TYPE_NUMERIC:
                         if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            Date date = cell.getDateCellValue();
                            if (date != null) {
                                value = new SimpleDateFormat("yyyy-MM-dd")
                                       .format(date);
                            } else {
                                value = "";
                            }
                         } else {
                            value = new DecimalFormat("0").format(cell
                                   .getNumericCellValue());
                         }
                         break;
                     case HSSFCell.CELL_TYPE_FORMULA:
                         // 导入时如果为公式生成的数据则无值
                    	 try{
                    		 value = String.valueOf(cell.getNumericCellValue());
                    	 }catch(IllegalStateException e){
                    		 value = String.valueOf(cell.getRichStringCellValue());
                    		 System.out.println("Excel 公式:"+value);
                    	 }                	 
                         /*if (!cell.getStringCellValue().equals("")) {
                            value = cell.getStringCellValue();
                         } else {
                            value = cell.getNumericCellValue() + "";
                         }*/
                         break;
                     case HSSFCell.CELL_TYPE_BLANK:
                         break;
                     case HSSFCell.CELL_TYPE_ERROR:
                         value = "";
                         break;
                     case HSSFCell.CELL_TYPE_BOOLEAN:
                         value = (cell.getBooleanCellValue() == true ? "Y"
                                : "N");
                         break;
                     default:
                         value = "";
                     }
                  }
                  if (columnIndex == 0 && value.trim().equals("")) {
                         break;
                      }
                  values[columnIndex] = rightTrim(value);
                  hasValue = true;
              }
 
              if (hasValue) {
                  result.add(values);
              }
              
           }

           in.close();
           
       }
       
       return result;
    }
   
    /**
     * 去掉字符串右边的空格
     * @param str 要处理的字符串
     * @return 处理后的字符串
     */
     public static String rightTrim(String str) {
       if (str == null) {
           return "";
       }
       int length = str.length();
       for (int i = length - 1; i >= 0; i--) {
           if (str.charAt(i) != 0x20) {
              break;
           }
           length--;
       }
       return str.substring(0, length);
    }
}
