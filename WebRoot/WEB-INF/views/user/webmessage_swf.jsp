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
    <script language="javascript" src="<%=path%>/resources/jquery/flexpaper_flash.js"></script>
    <script language="javascript" src="<%=path%>/resources/jquery/jquery-1.7.1.js"></script>
  </head>
  
<div>

<a id="viewerPlaceHolder" style="width:680px;height:480px;display:block"></a>
<input id="newFileName" type="hidden" value="<%=path%>/resources/report_temp_file/MVC.swf">      
<input id="FlexPaperViewer_path" type="hidden" value="<%=path%>/resources/report_temp_file/FlexPaperViewer"/>
<script type="text/javascript"> 
     	var filename = $("#newFileName").val();
     	var FlexPaperViewer_path= $("#FlexPaperViewer_path").val();
		var fp = new FlexPaperViewer(	
				 FlexPaperViewer_path,
				 'viewerPlaceHolder', { config : {
				 SwfFile : escape(newFileName),
				 Scale : 0.6, 
				 ZoomTransition : 'easeOut',
				 ZoomTime : 0.5,
				 ZoomInterval : 0.2,
				 FitPageOnLoad : true,
				 FitWidthOnLoad : false,
				 FullScreenAsMaxWindow : false,
				 ProgressiveLoading : false,
				 MinZoomSize : 0.2,
				 MaxZoomSize : 5,
				 SearchMatchAll : false,
				 InitViewMode : 'Portrait',
				 PrintPaperAsBitmap : false,
				 
				 ViewModeToolsVisible : true,
				 ZoomToolsVisible : true,
				 NavToolsVisible : true,
				 CursorToolsVisible : true,
				 SearchToolsVisible : true,
						
				 localeChain: 'zh_CN'
		}});
</script>
</div>
  


</html>
