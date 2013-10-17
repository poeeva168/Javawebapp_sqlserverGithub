<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'HelloWorld.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
    This is my JSP page. <br>
    
  	<hr>Testpojo_username  ：${Testpojo.username} , Testpojo_password  ：${Testpojo.password} </hr> 
  	<p/>
    <% 
    	out.println("path: "+path +" basePath: "+basePath);
    	out.println("Message:"+request.getAttribute("Message")+"<p><hr>" ); 
    	    	    	
    	for(Enumeration e  =  request.getAttributeNames(); e.hasMoreElements();)
    	{
			 Object o  =  e.nextElement();
			 out.println((String)o + " : "+request.getAttribute((String)o)+"<br><hr>");
    	} 
    
    
    %>
    
  </body>
</html>
