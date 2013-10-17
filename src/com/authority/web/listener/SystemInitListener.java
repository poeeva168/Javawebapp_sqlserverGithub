package com.authority.web.listener;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.authority.common.springmvc.SpringContextHolder;
import com.authority.pojo.Criteria;
import com.authority.service.BaseFieldsService;

import config.Classpathlocation;

/**
 * 系统初始化监听器
 * 
 * @author chenxin
 * @date 2011-12-16 下午11:26:14
 */
public class SystemInitListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		BaseFieldsService baseFieldsService = SpringContextHolder.getBean("baseFieldsServiceImpl");
		Criteria criteria = new Criteria();
		criteria.setOrderByClause(" field desc ,sort asc ");
		criteria.put("enabled", "1");
		servletContext.setAttribute("fields", baseFieldsService.selectAllByExample(criteria));
		
		/**监控文件*/		
		/*String classpath=Classpathlocation.class.getResource("").getPath();
		System.out.println("-------classpath:"+classpath);		
		FileObserver ob = new FileObserver(classpath);
		FileListener listener = new FileListener();
		ob.addListener(listener);
		FileMonitor monitor = new FileMonitor(ob);
		monitor.start();*/
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

}
