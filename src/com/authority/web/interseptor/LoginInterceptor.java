package com.authority.web.interseptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.authority.common.springmvc.SpringContextHolder;
import com.authority.pojo.BaseUsers;

/**
 * 验证用户登陆拦截器
 * 
 * @author chenxin
 * @date 2011-3-13 下午09:02:00
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		//服务器中所有的session信息:********
		Enumeration e=request.getSession().getAttributeNames();
		BaseUsers temp=new BaseUsers();
		for(;e.hasMoreElements();){
			System.out.println(e.nextElement());
			
		}
		
		// 如果session中没有user对象
		if (null == request.getSession().getAttribute(WebConstants.CURRENT_USER)) {
			String requestedWith = request.getHeader("x-requested-with");
			// ajax请求
			if (requestedWith != null && "XMLHttpRequest".equals(requestedWith)) {
				response.setHeader("session-status", "timeout");
				response.getWriter().print(WebConstants.TIME_OUT);
			} else {
				// 普通页面请求
				response.sendRedirect(request.getContextPath() + "/");
			}
			return false;
		}
		return true;

	}

}
