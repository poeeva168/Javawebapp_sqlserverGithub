package com.authority.service.impl;

import java.util.Date;

import javax.annotation.Resource;
import javax.jws.WebService;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.authority.common.springmvc.DateConvertEditor;
import com.authority.service.HelloWorldWS;

@WebService(endpointInterface="com.authority.service.HelloWorldWS",targetNamespace="http://www.henlo.net/HelloWorldWS")
public class HelloWorldWSImp implements HelloWorldWS {

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	@Override
	public String SayHello(String name) {
		System.out.println("Name:"+name);
		return "R:"+name;
	}

}
