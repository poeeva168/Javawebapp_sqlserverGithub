<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@page import="com.authority.pojo.WebMessage"%>
<%@page import="com.authority.common.utils.TypeCaseHelper"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="java.lang.reflect.Method"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<html>
  <head>
  	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>My JSP 'MyJsp.jsp' starting page</title>
    <script language="javascript" src="<%=path%>/resources/js/LodopFuncs.js"></script>
	<object id="LODOP_OB" classid="clsid:2105C259-1E0C-4534-8141-A753534CB4CA" width=0 height=0>
		<embed id="LODOP_EM" type="application/x-print-lodop" width=0 height=0 pluginspage="<%=path%>/resources/js/install_lodop32.exe"></embed>
	</object>
    <link rel="stylesheet" type="text/css" href="<%=path%>/resources/css/styles2.css" />
    <script language="javascript" type="text/javascript">
	    var LODOP; //声明为全局变量 	
		function prn3_preview(){	
			LODOP=getLodop(document.getElementById('LODOP_OB'),document.getElementById('LODOP_EM'));	
			LODOP.PRINT_INIT("打印控件功能演示_Lodop功能_全页");	
			LODOP.ADD_PRINT_HTM(0,0,"100%","100%",document.documentElement.innerHTML);	
			LODOP.PRINT_DESIGN();	
		};	
	</script>

  </head>
  
  <body> 
    <p>
    <% 
    	
    	List list =(List)request.getAttribute("Message");    	    
    %>    
    </p>
    <table>
    	<tr>
    		<td>
    			<% out.println("Message:"+request.getAttribute("Message")+"<p><hr>" );%> 
    		</td>
    	</tr>
    	<tr class="odd" >
    		<% for(Object obj:list){
    				Map map=(Map)obj;
    		%>
    		<td class="column1">标题</td>
    		<td class="column1"><%=map.get("HEAD")%></td>
    		<% } %>
    	</tr>
    	
    	<tr class="odd" >    		
    		<td class="column1" colspan="2">
    			<a href="javascript:prn3_preview()">打印预览</a>
    		</td>    		
    	</tr>

    </table>
        
  </body>
  


</html>
