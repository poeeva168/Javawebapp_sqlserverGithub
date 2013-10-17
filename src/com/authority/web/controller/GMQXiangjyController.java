package com.authority.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.authority.common.springmvc.DateConvertEditor;
import com.authority.pojo.ExceptionReturn;
import com.authority.pojo.ExtGridReturn;
import com.authority.pojo.ExtPager;
import com.authority.pojo.ExtReturn;
import com.authority.pojo.PdaReturn;
import com.authority.service.GMQXiangjyService;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.*;

import java.util.*;
import java.math.*;
import java.text.*;
import java.io.*;
import java.net.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import it.businesslogic.ireport.barcode.*;


@Controller
@RequestMapping("/portal_for_gmq/xiangjy")
public class GMQXiangjyController  {
	private static final Logger logger = LoggerFactory.getLogger(GMQXiangjyController.class);
		
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@Autowired
	private GMQXiangjyService gmqxiangjyservice;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	
	/**
	 * index
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String xiangjy() {
		return "portal_for_gmq/xiangjy";
	}
	
	/**
	 * 查找所有的消息
	 */
	@RequestMapping(value="/all", method = RequestMethod.POST)
	@ResponseBody
	public Object all(ExtPager pager, HttpSession session, HttpServletRequest request) {
		
		String BOXNO = request.getParameter("BOXNO");
		String FROM = request.getParameter("FROM")==null?"":request.getParameter("FROM").toString();
		Map<String,Object> paramMap=new HashMap<String, Object>();
		
		String query_list = "select B.B_PO_BOXNO_ID,a.BOXNO,b.M_PRODUCTALIAS_ID, C.NO  M_PRODUCT_ALIAS_NO,B.M_PRODUCT_ID,d.NAME M_PRODUCT_NAME,D.VALUE M_PRODUCT_VALUE,e.VALUE1_CODE color_code,e.value1 color_value,e.value2_code size_code,b.qty,0 QTY_QR "+
				"from B_PO_BOXNO a   "+
				"left join  B_PO_BOXITEM b on A.ID=B.B_PO_BOXNO_ID "+  
				"left join M_PRODUCT_ALIAS c on B.M_PRODUCTALIAS_ID=C.ID "+
				"left join M_PRODUCT d on B.M_PRODUCT_ID=D.ID "+
				"left join M_ATTRIBUTESETINSTANCE e on B.M_ATTRIBUTESETINSTANCE_ID=E.ID "+
				"where nvl(A.TEST_STATUS,'1')='1' ";
		if(BOXNO==null||BOXNO.equals(""))
			BOXNO=".";
		
		if(StringUtils.isNotBlank(BOXNO)){
			query_list = query_list+ " and BOXNO='"+BOXNO+"' ";
		}
		
		String query_count = "select count(*) from ("+query_list+")";
		
		/*// 排序信息
		if (StringUtils.isNotBlank(pager.getDir()) && StringUtils.isNotBlank(pager.getSort())) {
			query_list=query_list+" order by "+pager.getSort()+" "+pager.getDir();				
		}*/
					
		// 设置分页信息
		if (pager.getLimit() != null && pager.getStart() != null) {
			String OracleEnd=String.valueOf(pager.getLimit() + pager.getStart());
			String OracleStart=String.valueOf(pager.getStart());
			
			String Oracle_Pagination_Head="select y.* from ( select z.*, rownum as oracleStart from (";
			String Oracle_Pagination_Tail=") z where rownum <= to_number(:OracleEnd) ) y where y.oracleStart > to_number(:OracleStart)";

			query_list=Oracle_Pagination_Head+query_list+Oracle_Pagination_Tail;
			
			paramMap.put("OracleStart", OracleStart);
			paramMap.put("OracleEnd", OracleEnd);
						
		}
		
		List<Map<String,Object>> list = njdbcTemplate.queryForList(query_list, paramMap);
		int total = njdbcTemplate.getJdbcOperations().queryForInt(query_count);
		if(list.size()==0&&FROM.equals("PDA")){
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("ID", "");
			list.add(map);
		}		
		
		logger.debug("total:{}", total);
		return new ExtGridReturn(total, list);
		
	}
	
	/**
	 * 根据箱号获取 款号 的装箱数量
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping("/boxqty")
	@ResponseBody
	public Object boxqty(HttpSession session, HttpServletRequest request) {
		String BOXNO= request.getParameter("BOXNO").toString();
		if(BOXNO!=null){
			String query = "select BOX_QTY from M_PRODUCT where id=( select M_PRODUCT_ID from B_PO_BOXNO where  boxno='"+BOXNO+"')";
			String boxqty = jdbcTemplate.queryForObject(query,String.class);			
			return new ExtReturn(true, boxqty);
		}
		return new ExtReturn(false, "失败！");
	}
	
	
	@RequestMapping("/barcode")
	@ResponseBody
	public Object barcode(HttpSession session, HttpServletRequest request){
		String M_PRODUCT_ALIAS_NO = request.getParameter("data").toString();
		M_PRODUCT_ALIAS_NO = M_PRODUCT_ALIAS_NO==null?"":M_PRODUCT_ALIAS_NO;
		String query = "select max(NO) NO from M_PRODUCT_ALIAS where nvl(forcode,no)= '"+M_PRODUCT_ALIAS_NO+"'";
		String barcode = jdbcTemplate.queryForObject(query, String.class);
		if(barcode==null||barcode.equals(""))
			;
		else
			M_PRODUCT_ALIAS_NO = barcode;
		
		return new ExtReturn(true, M_PRODUCT_ALIAS_NO);
	}
	
		
	/**
	 * 检验结果提交
	 */
	@RequestMapping("/save")
	@ResponseBody
	public Object save(HttpSession session, HttpServletRequest request) {
		try {
			//更新 B_PO_BOXITEM 表
			String data = request.getParameter("data");
			List list  = JSON.parseArray(data);
			String result = "00";
			if(list.size()>0)
				result = gmqxiangjyservice.save(list);
			
			if ("01".equals(result)) {
				return new ExtReturn(true, "更新成功！");
			} else if ("00".equals(result)) {
				return new ExtReturn(false, "更新失败！");
			} else {
				return new ExtReturn(false, result);
			}
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
	}
	
	@RequestMapping(value="/henlo_ireport_xiangjy")
	@ResponseBody
	public void henlo_ireport_xiangjy(ExtPager pager, HttpSession session, HttpServletRequest request,HttpServletResponse response) throws IOException{

			PrintWriter writer = response.getWriter();
		try {
			
			String BOXNO =request.getParameter("param").toString();
			String query = "select max(id) from B_PO_BOXNO where BOXNO='"+BOXNO+"'";
			String objectid = jdbcTemplate.queryForObject(query, String.class);
			
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("objectid", Integer.parseInt(objectid));	
			
			// JasperPrint jasperPrint = new
			// JasperPrintWithConnection(reportFilePath, params,
			// con).getJasperPrint();

			String JASPER_FILE_PATH = request.getSession().getServletContext()
					.getRealPath("/WEB-INF/reports/");
			params.put("SUBREPORT_DIR", JASPER_FILE_PATH+"/");
			
			
			String JASPER_FILE_NAME = request.getSession().getServletContext()
					.getRealPath("/WEB-INF/reports/GMQ_XIANGBQ.jasper");
			// JasperReport jasperReport =
			// (JasperReport)JRLoader.loadObject(JASPER_FILE_NAME);
			File reportFile = new File(JASPER_FILE_NAME);
			InputStream in = new FileInputStream(reportFile);
			Connection conn = jdbcTemplate.getDataSource().getConnection();
			JasperPrint print = JasperFillManager.fillReport(in, params,conn);
			conn.close();
			// 使用JRHtmlExproter导出Html格式
			JRHtmlExporter exporter = new JRHtmlExporter();
			
			//设置图片文件存放路径，此路径为服务器上的绝对路径 			
		//	System.out.println(request.getSession().getServletContext().getRealPath("/"));
			
			String imageDIR = request.getSession().getServletContext().getRealPath("/")+"/resources/report_temp_file/";
			exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, imageDIR);
			
			//设置图片请求URI 
			
			String imageURI =  request.getContextPath() + "/resources/report_temp_file/"; 
			exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, request.getContextPath()+"/servlets/image?image="); 
			
			//设置导出图片到图片存放路径 
			exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.TRUE); 
			exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
			
			//设置导出对象 
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
			
			//设置导出方法 
			exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, response.getWriter());
			
			//设置HTTP Head 
			response.setContentType("text/html");
			request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE,print);

			// 导出
			exporter.exportReport();
			
		} catch (Exception e) {
			e.printStackTrace();
			writer.write("{success:false,errors:{msg:'打开失败'}}");
			writer.close();
			return;
		}
		// writer.write("{success:false,errors:{msg:'保存失败'}}");
		writer.close();

	}
	
	
}
