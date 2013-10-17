package com.authority.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.authority.common.springmvc.DateConvertEditor;
import com.authority.common.springmvc.SpringContextHolder;
import com.authority.common.utils.StringprocessHelper;
import com.authority.common.utils.TypeCaseHelper;
import com.authority.pojo.Article;
import com.authority.pojo.BaseUsers;
import com.authority.pojo.Criteria;
import com.authority.pojo.ExceptionReturn;
import com.authority.pojo.ExtGridReturn;
import com.authority.pojo.ExtPager;
import com.authority.pojo.ExtReturn;
import com.authority.pojo.WebMessage;
import com.authority.service.BaseFieldsService;
import com.authority.service.WebMessageService;
import com.authority.service.impl.ArticleClient;
import com.authority.web.interseptor.WebConstants;

@Controller
@RequestMapping("/message")
public class WebMessageController  {
	private static final Logger logger = LoggerFactory.getLogger(WebMessageController.class);
	@Autowired
	private WebMessageService webMessageservice;
	
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
	public String webmessage() {
		return "user/webmessage";
	}
	
	/**
	 * 查找所有的消息
	 */
	@RequestMapping(value="/all", method = RequestMethod.POST)
	@ResponseBody
	public Object all(ExtPager pager, @RequestParam(required = false) String head,HttpSession session, HttpServletRequest request) {
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
		if (StringUtils.isNotBlank(head)) {
			criteria.put("headLike", head);
		}
		
		BaseUsers baseUser = (BaseUsers)session.getAttribute(WebConstants.CURRENT_USER);
		
		if (StringUtils.isNotBlank(baseUser.getUserId())) {
			criteria.put("userid", baseUser.getUserId());
		}		
		
//		ArticleClient articleClient_=(ArticleClient)SpringContextHolder.getBean("articleClient");
//		Article article_ = articleClient_.getArticle("fun", 1);
//		System.out.println("Article: " + article_.getBody());
		
		List<WebMessage> list = this.webMessageservice.selectByExample(criteria);
		int total = this.webMessageservice.countByExample(criteria);
		logger.debug("total:{}", total);
		return new ExtGridReturn(total, list);
		
	}
	
	/**
	 * 查看是否有新消息
	 */
	@RequestMapping("/news")
	@ResponseBody
	public Object news(HttpSession session, HttpServletRequest request) {
		try {
			Criteria criteria = new Criteria();
			
			BaseUsers baseUser = (BaseUsers)session.getAttribute(WebConstants.CURRENT_USER);			
			
					
			/*String rowsdetail=request.getParameter("rowsdetail");
			String rowsnum =request.getParameter("rowsnum");
			
			System.out.println("rowsdetail:"+rowsdetail);
			System.out.println("rowsnum:"+rowsnum);
			String[] rows = rowsdetail.split("#");
			
			if(rows.length!=Integer.parseInt(rowsnum))
				System.out.println("行号数量不一致");
			
			for (int i = 0; i < rows.length; i++) {
				String[] cells = rows[i].split(",");				
				for (int j = 0; j < cells.length; j++) {
					String string = cells[j];
					System.out.print(string+",");					
				}
				System.out.println("");
			}*/
			
			
			
			
			if (StringUtils.isNotBlank(baseUser.getUserId())) {
				criteria.put("userid", baseUser.getUserId());
				criteria.put("isread", 1);
			}
			
			int total = this.webMessageservice.countByExample(criteria);
			logger.debug("total:{}", total);
			return new ExtReturn(true,total);
		}catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
		
	}
	/**
	 * 删除该消息
	 */
	@RequestMapping("/del/{id}")
	@ResponseBody
	public Object delete(@PathVariable List<String> id, HttpSession session) {
		try {
			
			/*if (StringUtils.isBlank(id)) {
				return new ExtReturn(false, "主键不能为空！");
			}
			Criteria criteria = new Criteria();
			criteria.put("id", id);	*/
			
			if(id.size()==0){
				return new ExtReturn(false, "主键不能为空！");
			}
			String result = this.webMessageservice.deleteByPrimaryKey_batch(id);
			
			if ("01".equals(result)) {
				return new ExtReturn(true, "删除成功！");
			} else if ("00".equals(result)) {
				return new ExtReturn(false, "删除失败！");
			} else {
				return new ExtReturn(false, result);
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
	}
	
	/**
	 * 保存消息
	 */
	@RequestMapping("/save")
	@ResponseBody
	public Object save(WebMessage mes, @RequestParam String id,HttpSession session, HttpServletRequest request) {
		try {
		
			String result="";
			Criteria criteria = new Criteria();
			BaseUsers baseUser = (BaseUsers)session.getAttribute(WebConstants.CURRENT_USER);
						
			mes.setRelease_time(new Date());
			mes.setRelease_per(baseUser.getAccount());
			mes.setRelease_ip(new LoginController().getIpAddr(request));
			criteria.put("mes", mes);
			
			if (StringUtils.isBlank(id)) {
				result = this.webMessageservice.saveMes(criteria);
			}
			else{
				result = this.webMessageservice.updateByPrimaryKeySelective(mes);
			}
			 
			if ("01".equals(result)) {
				return new ExtReturn(true, "保存成功！");
			} else if ("00".equals(result)) {
				return new ExtReturn(false, "保存失败！");
			} else {
				return new ExtReturn(false, result);
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
	}
	
	/**
	 * 保存消息
	 */
	@RequestMapping("/read/{id}")
	@ResponseBody
	public Object read(@PathVariable(value="id") String id,HttpSession session, HttpServletRequest request) {
		try {
		
			String result="";
			Criteria criteria = new Criteria();
			BaseUsers baseUser = (BaseUsers)session.getAttribute(WebConstants.CURRENT_USER);
						
			String user_id=baseUser.getUserId();
			String message_id=id;
			
			//已读处理
			String sql="insert into WEB_MESSAGE_USER(id,message_id,user_id) " +
						"select NEWID(),:message_id,:user_id  " +
						"where not exists(select 'x' from WEB_MESSAGE_USER b where b.message_id = :message_id and b.user_id = :user_id)";
			
			Map<String,Object> paramMap = new HashMap<String, Object>() ;	
			
			paramMap.put("user_id", user_id);
			paramMap.put("message_id", message_id);
			
			
			if(njdbcTemplate.update(sql,paramMap)>0)
				result="01";
			else
				result="01";
			
			/*String sql="insert into WEB_MESSAGE_USER(id,message_id,user_id) " +
			"select sys_guid(),?,? from dual a " +
			"where not exists(select 'x' from WEB_MESSAGE_USER b where b.message_id =? and b.user_id =?)";
			
			Object[] args={message_id,user_id,message_id,user_id};
			
			if(jdbcTemplate.update(sql, args)>0)
				result="01";
			else 
				result ="01";*/
			 
			if ("01".equals(result)) {
				return new ExtReturn(true, "保存成功！");
			} else if ("00".equals(result)) {
				return new ExtReturn(false, "保存失败！");
			} else {
				return new ExtReturn(false, result);
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
	}
	
	/**
	 * 导出EXCEL
	 */
	@RequestMapping("/xls")
	@ResponseBody
	public void xls(ExtPager pager, @RequestParam(required = false) String head,HttpSession session, HttpServletRequest request,HttpServletResponse response) {
		try {
			Criteria criteria = new Criteria();
			//排序信息			
			criteria.setOrderByClause("RELEASE_TIME");
			
			if (StringUtils.isNotBlank(head)) {
				criteria.put("headLike", head);
			}
			
			BaseUsers baseUser = (BaseUsers)session.getAttribute(WebConstants.CURRENT_USER);
			
			if (StringUtils.isNotBlank(baseUser.getUserId())) {
				criteria.put("userid", baseUser.getUserId());
			}		
			
			
			
			
			List<WebMessage> list = this.webMessageservice.selectByExample(criteria);
			int total = this.webMessageservice.countByExample(criteria);
			logger.debug("total:{}", total);
			
			//返回普通list 实则为 HashMap 对象
			/*List list_temp = this.webMessageservice.selectlist(criteria);
			
			for (Iterator it = list_temp.iterator(); it.hasNext();) {
				Map object = (Map) it.next();
				for (Iterator iter = object.keySet().iterator(); iter
						.hasNext();) {
					String ds = (String) iter.next();
					object.get(ds);
					
				}
				
			}*/
			
			// 普通的jdbctemplate   
			/*
			String sql="select count(*) from WEB_MESSAGE";
			System.out.println("dao.getJdbcTemplate().queryForInt(sql)"+ jdbcTemplate.queryForInt(sql));
			*/
							
			//------------导出excel----------------			
			
			//1. String[] col_id  2.String[] col_name  3.List list  4. String filename 5. HttpServletResponse response
			
			String[] col_id={"NUM","HEAD","BEGIN_DATE","END_DATE","MXDX","RELEASE_TIME","RELEASE_IP","RELEASE_PER","ISDISPLAY"};
			String[] col_name={"序号","标题","开始时间","结束时间","面向对象","发布时间","发布人IP","发布人","是否显示"};
			String fileName="导出EXCEL-TEST";
			
			this.webMessageservice.Excel_pro(list,col_id,col_name,fileName,response);

		} catch (Exception e) {
			logger.error("Exception: ", e);
		}
	}
	
	@RequestMapping("/print")
	@ResponseBody
	public ModelAndView print(ExtPager pager,HttpSession session, HttpServletRequest request,HttpServletResponse response) {
		ModelAndView mav = new ModelAndView();
		try {
			Criteria criteria = new Criteria();
			//排序信息			
			criteria.setOrderByClause("RELEASE_TIME");
			
			String id=request.getParameter("id");
			
			if (StringUtils.isNotBlank(id)) {
				criteria.put("id", id);
			}
			
			BaseUsers baseUser = (BaseUsers)session.getAttribute(WebConstants.CURRENT_USER);
			
			if (StringUtils.isNotBlank(baseUser.getUserId())) {
				criteria.put("userid", baseUser.getUserId());
			}	

			List<HashMap<String,Object>> list = this.webMessageservice.selectByDynamicSql(criteria);
			
			mav.setViewName("user/webmessage_print");
			mav.addObject("Message", list);
			
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				
				HashMap<String, Object> hashMap = (HashMap<String, Object>) iterator.next();
				
			//	String[] key=(String[]) hashMap.keySet().toArray();
				
				Set<String> key_set = hashMap.keySet();
				
				for (String string : key_set) {
					System.out.println("key:"+string);
					System.out.println("value:"+hashMap.get(string));
				}
					
			}
			

		} catch (Exception e) {
			logger.error("Exception: ", e);
		}

		return mav;
		
	}
	
	@RequestMapping("/swf")
	@ResponseBody
	public ModelAndView swf(ExtPager pager,HttpSession session, HttpServletRequest request,HttpServletResponse response) {
		ModelAndView mav = new ModelAndView();
		try {
			Criteria criteria = new Criteria();
			//排序信息			
			criteria.setOrderByClause("RELEASE_TIME");
			
			String id=request.getParameter("id");
			
			if (StringUtils.isNotBlank(id)) {
				criteria.put("id", id);
			}
			
			BaseUsers baseUser = (BaseUsers)session.getAttribute(WebConstants.CURRENT_USER);
			
			if (StringUtils.isNotBlank(baseUser.getUserId())) {
				criteria.put("userid", baseUser.getUserId());
			}	

			List<HashMap<String,Object>> list = this.webMessageservice.selectByDynamicSql(criteria);
			
			mav.setViewName("user/webmessage_swf");
			mav.addObject("Message", list);			
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
		}

		return mav;
		
	}
}
