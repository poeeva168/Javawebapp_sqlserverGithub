package com.authority.web.controller;

import java.io.File;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.portlet.ModelAndView;

import com.authority.common.jackjson.JackJson;
import com.authority.common.springmvc.DateConvertEditor;
import com.authority.common.utils.FileDigest;
import com.authority.common.utils.FileOperateUtil;
import com.authority.pojo.Criteria;
import com.authority.pojo.ExtGridReturn;
import com.authority.pojo.ExtPager;
import com.authority.pojo.ExtReturn;
import com.authority.common.utils.WebUtils;

@Controller
@RequestMapping("/functiontest")
public class WebFunctiontestController {
	
	private static final Logger logger = LoggerFactory.getLogger(WebFunctiontestController.class);
	
	
	
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	/**
	 * index
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String webfunctiontest() {
		return "user/webfunctiontest";
	}
	
	@RequestMapping(value="/dynamicgrid_store")
	@ResponseBody
	public Object dynamicgrid_store(ExtPager pager, HttpSession session, HttpServletRequest request){
		Criteria criteria = new Criteria();
		// 设置分页信息
		if (pager.getLimit() != null && pager.getStart() != null) {
			criteria.setOracleEnd(pager.getLimit() + pager.getStart());
			criteria.setOracleStart(pager.getStart());
		}
		// 排序信息
		if (StringUtils.isNotBlank(pager.getDir()) && StringUtils.isNotBlank(pager.getSort())) {
			criteria.setOrderByClause(pager.getSort() + " " + pager.getDir());
		}
		
		String sql_data="select * from (select 'ID' YXGSID,'代码(YXGS)' YXGSM,'名称(YXGS)' YXGS_QUANC,'名称(MC)' MAIC_QUANC,'1' TITLE from dual "+ 
				   "union all "+
				   "select YXGSID,YXGSM, YXGS_QUANC,MAIC_QUANC,'0' TITLE from WEB_QUDVIEW where MAICM='C00725') where title=0";
		
		String sql_title="select * from (select 'ID' YXGSID,'代码(YXGS)' YXGSM,'名称(YXGS)' YXGS_QUANC,'名称(MC)' MAIC_QUANC,'1' TITLE from dual "+ 
				   "union all "+
				   "select YXGSID,YXGSM, YXGS_QUANC,MAIC_QUANC,'0' TITLE from WEB_QUDVIEW where MAICM='C00725') where title=1";
		
		//传入 jdbcTemplate sql_data sql_title 返回  metaData  数据		
		
		List list=jdbcTemplate.queryForList(sql_data);
		
		int total=list.size();		
		
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sql_title);
		
		WebUtils web=new WebUtils();
		
		@SuppressWarnings("unchecked")
		Map<String,Object> metaData=(Map<String, Object>) web.getmetaData(rs);
		
		
		return new ExtGridReturn(total, list,metaData); 
		
	}
	
	
	@SuppressWarnings("finally")
	@RequestMapping(value="/uploadFiles")
	@ResponseBody
	public ModelAndView  uploadFiles(@RequestParam MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String data="";
		
		try {
		//	System.out.println("CURRENT_USER"+request.getSession().getAttribute(WebConstants.CURRENT_USER));	
			//文件的MD5
			logger.info("start");
			
			String fileMD5=FileDigest.getFileMD5(file.getInputStream());
			logger.info(fileMD5);
			// 保存的地址
			String savePath = request.getSession().getServletContext().getRealPath("/upload");
			// 上传的文件名 //需要保存
			String uploadFileName = file.getOriginalFilename();
			// 获取文件后缀名 //需要保存
			String fileType = StringUtils.substringAfterLast(uploadFileName, ".");
			logger.debug("文件的MD5：{},上传的文件名：{},文件后缀名：{},文件大小：{}",
					new Object[] {fileMD5, StringUtils.substringBeforeLast(uploadFileName, "."), fileType, file.getSize() });
			// 以年月/天的格式来存放
			String dataPath = DateFormatUtils.format(new Date(), "yyyy-MM" + File.separator + "dd");
			
			dataPath=File.separator+"/tempexcel/";
			
			String finalPath=dataPath+StringUtils.substringBeforeLast(uploadFileName, ".")+("".equals(fileType) ? "" : "." + fileType);
									
			logger.debug("savePath:{},finalPath:{}", new Object[] { savePath, finalPath });
			File saveFile_tmp = new File(savePath + finalPath);
			// 判断文件夹是否存在，不存在则创建
			if (!saveFile_tmp.getParentFile().exists()) {
				saveFile_tmp.getParentFile().mkdirs();
			}
			
			File saveFile = new File(saveFile_tmp.getParent()+File.separator+FileOperateUtil.checkFileName(saveFile_tmp.getName(),saveFile_tmp.getParent()));
			
			// 写入文件
			FileUtils.writeByteArrayToFile(saveFile, file.getBytes());
			// 保存文件的基本信息到数据库
			// 上传的文件名（带不带后缀名？）；文件后缀名；存放的相对路径
			
//			System.out.println("getFreeSpace:"+saveFile.getFreeSpace());
//			System.out.println("getTotalSpace:"+saveFile.getTotalSpace());
//			System.out.println("getUsableSpace:"+saveFile.getUsableSpace());
			
			
			String returnMsg = JackJson.fromObjectToJson(new ExtReturn(true, "磁盘剩余空间："+(saveFile.getUsableSpace() / 1073741824f)+" GB"));
			logger.debug("{}", returnMsg);
			
			response.setContentType("text/html;charset=utf-8");
			data="{success:true,msg:'成功'}";

		} catch (Exception e) {
			logger.error("Exception: ", e);
		}finally{
			response.getWriter().write(data);
			response.getWriter().flush();
			
			return null;
		}
		
		
	}

}
