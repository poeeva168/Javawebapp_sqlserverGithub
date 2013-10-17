<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div id="${param.id}"></div>
<iframe id='xls' name='xls' style='display:none'></iframe>
<div align="center">
<table  >
	<tr>
		<td ><a href="resources/mould/mjn_excel.xlsx" target="xls" >Excel 模板下载</a> </td>
	</tr>
	<tr>
		<td ><input type="button" value="Excel 数据上传 " id="mjn_dataupload_uploads"/></td>
	</tr>
	<tr>
		<td >提示信息：<span id = "ext_msg" ></span></td>
	</tr>
	<tr>
		<td >处理结果：<span id = "ext_object" ></span></td>
	</tr>
</table>
</div>
<script type="text/javascript" >
yepnope({
	load : [	 	 
 
		],
	complete : function() {
			
	Ext.ns("Ext.Authority.mjn_dataupload"); // 自定义一个命名空间
	mjn_dataupload = Ext.Authority.mjn_dataupload; // 定义命名空间的别名
	mjn_dataupload = {
		uploadFiles_url : ctx + '/mjn_dataupload/uploadFiles',// 文件上传
		dynamicgrid_store_url : ctx + '/mjn_dataupload/dynamicgrid_store',
		column_update : 0,
		isdisplay : eval('(${fields.isdisplay==null?"{}":fields.isdisplay})'),
		role : eval('(${fields.role==null?"{}":fields.role})'),
		user_role : "${sessionScope.CURRENT_USER_ROLE}",
		pagesizes:eval('(${fields.pagesizes==null?"{}":fields.pagesizes})'),
		pageSize : 20 // 每页显示的记录数
		
	};
	//document.getElementById("ext_msg").innerHTML="...";
	
	mjn_dataupload.showUploadDialog = function(){
	
		var UploadForm = new Ext.form.FormPanel({  
	     	 id : 'UploadForm',
	     	 height : 120,
	     	 width : 350, 
	     	 labelWidth: 70,
	         frame : true,  
	         monitorValid:true,  
	         fileUpload:true, //需上传文件  
	         url : mjn_dataupload.uploadFiles_url+";jsessionid=${pageContext.session.id}",//请求的url地址  
	         method:'POST',
	     //  enctype:'multipart/form-data',
	         items : [
	         	{  
	             xtype:'textfield',  
	             fieldLabel : '选择文件',
	             
	             name : 'file',  
	             inputType : 'file',
	             allowBlank:false
	         	}
	         ],  
	         buttons : [{  
	         	 formBind:true,  
	             text : '上传',  
	             handler:function(){  
	                 UploadForm.form.submit({ 
	                 	 success : function(form, action) {//加载成功的处理函数  
	                 		 Ext.getDom("ext_msg").innerHTML=action.result.msg;
	                 	 	 var downloadfile = encodeURI(action.result.o);
	                 		 Ext.getDom("ext_object").innerHTML="<a href='resources/upload/mjn_files/"+downloadfile+"'>"+action.result.o+"</a>";
	                 	 	//Ext.Msg.alert(action.result.msg);
	                     },  
	                     failure : function(form, action) {//加载失败的处理函数  
		                        Ext.Msg.alert('Error',
		                            'Status:'+action.status+': '+action.result.msg);
	                     }
	                 });  
	              }  
	         }, {  
	             text : '关闭',  
	             handler:function(){  
	                 UploadWin.close();  
	             }  
	         }]
	     });
		
		var UploadWin = new Ext.Window({
			title : '数据同步上传管理',
			layout : 'fit',
			items : [UploadForm]
		});
		
		UploadWin.show();
	};
	
	$("#mjn_dataupload_uploads").click(function(){
		mjn_dataupload.showUploadDialog();
	});	
	
	}
	
});



	   				
</script>
