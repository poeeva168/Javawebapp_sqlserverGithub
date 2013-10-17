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
import com.authority.pojo.ExceptionReturn;
import com.authority.pojo.ExtGridReturn;
import com.authority.pojo.ExtPager;
import com.authority.pojo.ExtReturn;
import com.authority.common.utils.WebUtils;

@Controller
@RequestMapping("/mjn_dataupload")
public class Mjn_datauploadController {
	
	private static final Logger logger = LoggerFactory.getLogger(Mjn_datauploadController.class);
	
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
	public String mjn_dataupload() {
		return "user/mjn_dataupload";
	}
			
	@SuppressWarnings("finally")
	@RequestMapping(value="/uploadFiles")
	@ResponseBody
	public ModelAndView uploadFiles(@RequestParam MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Object> data =new HashMap<String, Object>();		
		data.put("success", false);		
		String returnMsg = "";
		ExtReturn ext = new ExtReturn();
		ext.setSuccess(false);
		ext.setMsg("处理失败……");
		try {
		//	System.out.println("CURRENT_USER"+request.getSession().getAttribute(WebConstants.CURRENT_USER));	
			//文件的MD5
			logger.info("start");
			
			// 保存的地址
			String savePath = request.getSession().getServletContext().getRealPath("/resources/upload");
			// 上传的文件名 //需要保存
			String uploadFileName = file.getOriginalFilename();
			// 获取文件后缀名 //需要保存
			String fileType = StringUtils.substringAfterLast(uploadFileName, ".");
			
			String dataPath=File.separator+"/mjn_files/"; 
			
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
			ext.setSuccess(true);
			ext.setMsg("处理成功……");
			ext.setO(uploadFileName);
			response.setContentType("text/html;charset=utf-8");
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
		} finally {
			
			returnMsg = JackJson.fromObjectToJson(ext);
			response.getWriter().write(returnMsg);
			response.getWriter().flush();
			
			return null;
		}
		
	}

}
