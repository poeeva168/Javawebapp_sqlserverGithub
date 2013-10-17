<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<title><fmt:message key="login.title" /></title>
<%@ include file="/WEB-INF/views/commons/yepnope.jsp"%>
<script type="text/javascript">
yepnope({
	load : ["${ctx}/resources/css/icons.css","${ctx}/resources/js/main.js"],
	complete : function() {
		Ext.getCmp('username').setValue('${user.realName }');
	}
});	
</script>

<script type="text/javascript" src="${pageContext.request.contextPath }/dwr/engine.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath }/dwr/util.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath }/dwr/interface/ChatService.js"></script>


</head>
<body class="x-border-layout-ct" style="position:static;overflow:hidden;" onload="dwr.engine.setActiveReverseAjax(true);">
<div></div>

</body>
</html>
