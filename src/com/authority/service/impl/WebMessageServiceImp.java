package com.authority.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.authority.common.utils.StringprocessHelper;
import com.authority.common.utils.TypeCaseHelper;
import com.authority.dao.WebMessageMapper;
import com.authority.pojo.Criteria;
import com.authority.pojo.WebMessage;
import com.authority.service.WebMessageService;

@Service
public class WebMessageServiceImp implements WebMessageService {

	@Autowired
	private WebMessageMapper webMessageMapper;
	
	private static final Logger logger = LoggerFactory.getLogger(WebMessageServiceImp.class);
	
	@Override
	public int countByExample(Criteria example) {
		int count = this.webMessageMapper.countByExample(example);
		logger.debug("count: {}", count);
		return count;
	}

	@Override
	public WebMessage selectByPrimaryKey(String id) {
		return this.webMessageMapper.selectByPrimaryKey(id);
	}

	@Override
	public List<WebMessage> selectByExample(Criteria example) {
		return this.webMessageMapper.selectByExample(example);
	}

	@Override
	public List<Object[]> selectforexcel(Criteria example) {
		return this.webMessageMapper.selectforexcel(example);
	}

	@Override
	public List selectlist(Criteria example) {
		// TODO Auto-generated method stub
		return this.webMessageMapper.selectlist(example);
	}

	@Override
	public List<HashMap<String, Object>> selectByDynamicSql(Criteria example) {
		// TODO Auto-generated method stub
		return this.webMessageMapper.selectByDynamicSql(example);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String deleteByPrimaryKey(Criteria criteria) {
		String id = criteria.getAsString("id");
		int result = 0;
		
		result = this.webMessageMapper.deleteByPrimaryKey(id);
		return result > 0 ? "01" : "00";
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String deleteByPrimaryKey_batch(List<String> list) {
		// TODO Auto-generated method stub
		int result = 0;
		
		result = this.webMessageMapper.deleteByPrimaryKey_batch(list);
		return result > 0 ? "01" : "00";
		
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String saveMes(Criteria example) {
		WebMessage mes=(WebMessage)example.get("mes");
		int result = 0;
		
		result = this.webMessageMapper.insertSelective(mes);
		return result > 0 ? "01" : "00";
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String updateByPrimaryKeySelective(WebMessage mes) {
		// TODO Auto-generated method stub
		int result = 0;
		
		result = this.webMessageMapper.updateByPrimaryKeySelective(mes);		
		
		return result > 0 ? "01" : "00";		
	}

	public void Excel_pro(List<WebMessage> list, String[] col_id,String[] col_name, String fileName,HttpServletResponse response) {
		// TODO Auto-generated method stub
		try {
						
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("sheet");
			//普通样式
			HSSFCellStyle   Style_default   =   wb.createCellStyle();
			Style_default.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER); 
			Style_default.setAlignment(HSSFCellStyle.ALIGN_LEFT);  
			// 设置边框
			Style_default.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			Style_default.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		    Style_default.setBorderRight(HSSFCellStyle.BORDER_THIN);
		    Style_default.setBorderTop(HSSFCellStyle.BORDER_THIN);			
		    //自动换行
//		    Style_default.setWrapText(true);
		    //字体设置		    
		    HSSFFont font_default = wb.createFont();
			font_default.setFontHeightInPoints((short)10);
			font_default.setFontName("宋体");
			//打印设置
	        HSSFPrintSetup ps = sheet.getPrintSetup();
	        ps.setLandscape(false); //打印方向，true:横向，false:纵向
	        ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); //纸张
	        sheet.setMargin(HSSFSheet.BottomMargin, (double)0.3); //页边距（下）
	        sheet.setMargin(HSSFSheet.LeftMargin, (double)0.3); //页边距（左）
	        sheet.setMargin(HSSFSheet.RightMargin, (double)0.3); //页边距（右）
	        sheet.setMargin(HSSFSheet.TopMargin, (double)0.3); //页边距（上）
	        sheet.setHorizontallyCenter(true); //设置打印页面为水平居中
	        
			Style_default.setFont(font_default);
			
			if(1>0){
				
				HSSFRow row=sheet.createRow(0);				
				//标题区
				for(int i=0;i<col_name.length;i++){
					HSSFCell cell = row.createCell(i);
					cell.setCellValue(new HSSFRichTextString(col_name[i]));
					cell.setCellStyle(Style_default);
				}
			}
			//数据区
			int num=1; //序号
			int row_num=1;
			
						
			for(Object obj:list){
				
				Map<String,Object> map=TypeCaseHelper.convert2Map(obj);
				
				
				HSSFRow row=sheet.createRow(row_num);
				
				for(int i=0;i<col_id.length;i++){
					
					HSSFCell cell = row.createCell(i);
					
					if(col_id[i].equals("NUM"))
						cell.setCellValue(new HSSFRichTextString(String.valueOf(num)));
					else{
						
						cell.setCellValue(new HSSFRichTextString(StringprocessHelper.String_html(String.valueOf(map.get(col_id[i].toLowerCase())))));
					}
					cell.setCellStyle(Style_default);
				}
				num++;
				row_num++;
			}
			
									
			response.setHeader("Content-disposition","attachment; filename="+new String(fileName.getBytes("GBK"),"ISO8859_1"));// 下载方式，下载，不用浏览器打开
			response.setContentType("application/vnd.ms-excel");			
			
			
			ServletOutputStream out = response.getOutputStream();		
				
			wb.write(out);		
			out.flush();
			out.close();
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
		}
		
	}

}
