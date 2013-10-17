package com.authority.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.authority.common.springmvc.DateConvertEditor;
import com.authority.pojo.ExceptionReturn;
import com.authority.pojo.ExtGridReturn;
import com.authority.pojo.ExtReturn;
import com.authority.service.GMQPdaService;
import com.authority.service.GMQXiangjyService;
import com.sun.org.apache.bcel.internal.generic.NEW;

@Controller
@RequestMapping("/portal_for_gmq/pdaaction")
public class GMQPdaActionController {
	private static final Logger logger = LoggerFactory.getLogger(GMQPdaActionController.class);
	
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@Autowired
	private GMQPdaService gmqpdaservice;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	@RequestMapping(value="/login_store")
	@ResponseBody
	public Object login_store(HttpSession session, HttpServletRequest request) {
		try {
			
			String query = "select id,name  from c_store a where isstop='N'  and exists(select 'x' from C_STOREATTRIBVALUE b where a.C_STOREATTRIB6_ID=b.id and upper(b.name) ='PDA'  )  order by name";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			int total = list.size();
			if(list.size()==0){
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("ID", "");
				list.add(map);
			}
			
			return new ExtGridReturn(total, list); 
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
		
	}
	
	@RequestMapping(value="/login_users")
	@ResponseBody
	public Object login_users(HttpSession session, HttpServletRequest request) {
		try {
			String C_STORE_ID = request.getParameter("C_STORE_ID").toString();
			String FROM = request.getParameter("FROM")==null?"":request.getParameter("FROM").toString();
			String query = "select distinct ID,TRUENAME||'*'||substr(email,0,(case when instr(EMAIL,'@')=0 then length(email) else instr(EMAIL,'@')-1 end)) name " +
					"from USERS where C_STORE_ID='"+C_STORE_ID+"' and  ISOPR ='Y' and ISENABLED ='1' ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			int total = list.size();
			if(list.size()==0&&FROM.equals("PDA")){
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("ID", "");
				list.add(map);
			}
			return new ExtGridReturn(total, list); 			
						
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}			
		
	}
	
	@RequestMapping(value="/login")
	@ResponseBody
	public Object login(HttpSession session, HttpServletRequest request) {
		try {
			String result = "00";
			String USERID = request.getParameter("USERID").toString();
			String PASSWORD = request.getParameter("PASSWORD").toString();
			
			String query = "select count(*) from USERS where id='"+USERID+"' and PASSWORDHASH='"+PASSWORD+"'";
			if(jdbcTemplate.queryForInt(query)>0)
				result ="01";
			
			if ("01".equals(result)) {
				return new ExtReturn(true, "成功！");
			} else if ("00".equals(result)) {
				return new ExtReturn(false, "用户名或密码错误！");
			} else {
				return new ExtReturn(false, result);
			}
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}			
		
	}
	
	@RequestMapping(value="/xiangck_check")
	@ResponseBody
	public Object xiangck_check(HttpSession session, HttpServletRequest request) {
		try {
			String result = "00",query="";
			String BOXNO =request.getParameter("BOXNO").toString();
			//1.该箱号是否已经被检验
			query = "select count(*) from b_po_boxno where TEST_STATUS=2 and boxno='"+BOXNO+"'";
			if(jdbcTemplate.queryForInt(query)==0)
				return new ExtReturn(false, "该箱还未检验！");			
			//2.该箱号是否已经被出库
			query = "select count(*) from M_ISSUE_BOX " +
					"where OUT_STATUS=2 and " +
					"b_po_boxno_id=(select id from b_po_boxno where TEST_STATUS=2 and boxno='"+BOXNO+"')";
			if(jdbcTemplate.queryForInt(query)>0)
				return new ExtReturn(false, "该箱已出库！");
			
			return new ExtReturn(true, "待出库");
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}			
		
	}
	
	@RequestMapping(value="/xiangck_submit")
	@ResponseBody
	public Object xiangck_submit(HttpSession session, HttpServletRequest request) {
		try {
			String data = request.getParameter("data");
			String userid = request.getParameter("userid");
			List list  = JSON.parseArray(data);
			String result = "00";
			if(list.size()>0)
				result = gmqpdaservice.xiangck_submit(list,userid);
			
			if ("01".equals(result)) {
				return new ExtReturn(true, "更新成功！");
			} else if ("00".equals(result)) {
				return new ExtReturn(false, "");
			} else {
				return new ExtReturn(false, result);
			}
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}			
		
	}
	
	
}
