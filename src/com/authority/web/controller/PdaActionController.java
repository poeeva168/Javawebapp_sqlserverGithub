package com.authority.web.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.authority.common.springmvc.DateConvertEditor;

/**
 * Pda Action 处理相关方法
 * @author Administrator
 *
 */

@Controller
@RequestMapping("/pdaaction")
public class PdaActionController {
	private static final Logger logger = LoggerFactory.getLogger(PdaActionController.class);
	
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
	 * 时间同步获取
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/timesyn")
	@ResponseBody
	public Object timesysn(HttpSession session, HttpServletRequest request){
		String sql="select to_char(sysdate-8/24,'YYYYMMDDHH24MISS') time from dual  ";
		System.out.println("====================");
		return jdbcTemplate.queryForList(sql).get(0);
				
	}
	
	
}
