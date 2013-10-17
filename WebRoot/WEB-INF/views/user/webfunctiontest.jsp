<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div id="${param.id}"></div>
<iframe id='xls' name='xls' style='display:none'></iframe>
<style type="text/css">
    	.x-grid-record-gray table{color: #948d8e;}
		.x-grid-record-red table{color: white; background: red;}
		.x-grid-record-yellow table{color: blue;}
		.x-grid-record-green table{color: green;}
		.x-grid-record-orange table{color: orange;}    
</style>
<script type="text/javascript" >
yepnope({
	load : [	 	 
         //Ext.ux.grid.RowEditor
         "${ctx}/resources/extjs/ux/css/RowEditor.css",
         "${ctx}/resources/extjs/ux/RowEditor.js",
         //swfupload
         "${ctx}/resources/swfupload/css/icons.css",
		 "${ctx}/resources/swfupload/swfupload.js",
		 "${ctx}/resources/swfupload/uploaderPanel.js"         
		],
	complete : function() {
			
	Ext.ns("Ext.Authority.functiontest"); // 自定义一个命名空间
	functiontest = Ext.Authority.functiontest; // 定义命名空间的别名
	functiontest = {
		uploadFiles_url : ctx + '/functiontest/uploadFiles',// 文件上传
		flashUrl : ctx + '/resources/swfupload/swfupload.swf', //文件上传flash地址
		dynamicgrid_store_url : ctx + '/functiontest/dynamicgrid_store',
		column_update : 0,
		isdisplay : eval('(${fields.isdisplay==null?"{}":fields.isdisplay})'),
		role : eval('(${fields.role==null?"{}":fields.role})'),
		user_role : "${sessionScope.CURRENT_USER_ROLE}",
		pagesizes:eval('(${fields.pagesizes==null?"{}":fields.pagesizes})'),
		pageSize : 20 // 每页显示的记录数
		
	};
	
	/**  动态列  begin*/
	/** 改变页的combo */
	functiontest.pageSizeCombo = new Share.pageSizeCombo({
				value : '20',
				listeners : {
					select : function(comboBox) {
						functiontest.pageSize = parseInt(comboBox.getValue());
						functiontest.bbar.pageSize = parseInt(comboBox.getValue());
						functiontest.store.baseParams.limit = functiontest.pageSize;
						functiontest.store.baseParams.start = 0;
						functiontest.store.load();
					}
				}
			});

	functiontest.pageSize = parseInt(functiontest.pageSizeCombo.getValue());
	/** 基础 数据源Field、grid columns信息 */
	functiontest.field=['YXGSID'];
	
	functiontest.columns=[
		new Ext.grid.RowNumberer()
	];
	
	functiontest.showUploadDialog = function(){
	
		var UploadForm = new Ext.form.FormPanel({  
	     	 id : 'UploadForm',
	     	 height : 80,
	         frame : true,  
	         monitorValid:true,  
	         fileUpload:true, //需上传文件  
	         url : functiontest.uploadFiles_url+";jsessionid=${pageContext.session.id}",//请求的url地址  
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
	                         Ext.Msg.alert(action.result.msg);
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
			title : '上传示例',
			layout : 'fit',
			items : [UploadForm]
		});
		
		UploadWin.show();
	};
	/** ----------Action 行为组-------------begin*/
	functiontest.actions = [			
			new Ext.Action({
				text : '上传 Excel',
				iconCls : 'db-icn-upload',
				handler : function() {
					functiontest.showUploadDialog();
				},
				scope : this
			})
		];
	
	
	/** ----------Action 行为组-------------end*/
	/** 基本信息-数据源 */
	functiontest.store = new Ext.data.Store({
		//		autoLoad : true,
				remoteSort : true,
				baseParams : {
					start : 0,
					limit : functiontest.pageSize
				},
				proxy : new Ext.data.HttpProxy({// 获取数据的方式
					method : 'POST',
					url : functiontest.dynamicgrid_store_url
				}),
				reader : new Ext.data.JsonReader({// 数据读取器
					totalProperty : 'results', // 记录总数
					root : 'rows' // Json中的列表数据根节点
					
				}), //fields:fd
				listeners : {
					'load' : function(store, records, options) {
						//组合动态列信息					
						/*column={header: 'YXGSM', dataIndex: 'YXGSM'};
						var config = functiontest.grid.getColumnModel().config;
						config.push(column);		
						functiontest.grid.getColumnModel().setConfig(config);	*/					
					},
					'metachange':function(store, meta){
					   	
					   	if(functiontest.column_update==0){
					   		var columnInfos=meta.columns;
						   	var config = functiontest.grid.getColumnModel().config;							   			
						   	Ext.each(columnInfos, function(column) {
						   		//alert(column.name);			
						   		config.push(column);			   	
						   	});
						   	
						   	var column_icon={header: '图标', dataIndex: '',
						   		renderer : function(_v, cellmeta, record){
						   		if(record.data.YXGSM=='02')
						   			return '<div style="background:url(resources/images/icons/stop.png);height: 16px;background-repeat: no-repeat;">&nbsp;</div>';						   		
						   		else
						   			return '';	
						   		
						   	}};
						   	config.push(column_icon);
						   	functiontest.grid.getColumnModel().setConfig(config);
						   	functiontest.column_update=1;
					   	}
					   	
					   	/*var ColumnCount=functiontest.grid.getColumnModel().getColumnCount();
					   	for(var i=0;i<ColumnCount;i++){
							alert(functiontest.grid.getColumnModel().getColumnId(i));			
						}*/
					   	
					}
				}
			});
	/** 基本信息-选择模式 */
	functiontest.selModel = new Ext.grid.CheckboxSelectionModel({
			singleSelect : true			
		});
	/** 基本信息-数据列 */
	functiontest.colModel = new Ext.grid.ColumnModel({
			defaults : {
				sortable : true,
				width : 140
			},
			columns : functiontest.columns
		});	
	
	/** 顶部工具栏*/
	functiontest.tbar = [
		functiontest.actions[0]
	];
	
	
	
		
	/** 底部工具条 */
	functiontest.bbar = new Ext.PagingToolbar({
			pageSize : functiontest.pageSize,
			store : functiontest.store,
			displayInfo : true,
			// plugins : new Ext.ux.ProgressBarPager(), // 分页进度条
			items : ['-', '&nbsp;', functiontest.pageSizeCombo]
		});	
		
	functiontest.grid = new Ext.grid.GridPanel({
			store : functiontest.store,
			colModel : functiontest.colModel,
			selModel : functiontest.selModel,
			tbar : functiontest.tbar,
			bbar : functiontest.bbar,
			autoScroll : 'auto',
			region : 'center',
			loadMask : true,
			// autoExpandColumn :'fieldDesc',
			//plugins: [functiontest.editor],
			stripeRows : true,
			listeners : {},
			viewConfig : {
				forceFit : true,
				getRowClass:function(record,index,p,ds) {
				      var cls = 'white-row';
				         switch (record.data.YXGSM) {
				             case '00' :     cls = 'x-grid-record-green';    break;
				             case '01' :     cls = 'x-grid-record-yellow';     break;
				             case '02' :     cls = '';      break;
				             case '03' :     cls = 'x-grid-record-orange';       break;
				             case '04' :     cls = 'x-grid-record-gray';      break;
				         }
				         return cls;
				}
			}
		});
	
	/**  节点详细信息  以及节点直接下属信息 end*/
	functiontest.myPanel = new Ext.Panel({
		id : '${param.id}' + '_panel',
		renderTo : '${param.id}',
		layout : 'border',
		boder : false,
		height : index.tabPanel.getInnerHeight() - 1,
		items : [functiontest.grid]
		});
	
	
	}
	
});



	   				
</script>
