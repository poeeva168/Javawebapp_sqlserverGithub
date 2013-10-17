	
Ext.ns("Ext.Authority.message"); // 自定义一个命名空间
message = Ext.Authority.message; // 定义命名空间的别名
message = {
	all : ctx + '/message/all',// 加载所有
	save : ctx + "/message/save",//保存
	del : ctx + "/message/del/",//删除
	xls : ctx + "/message/xls" ,//EXCEL
	isdisplay : eval('(${fields.isdisplay==null?"{}":fields.isdisplay})'),
	pagesizes:eval('(${fields.pagesizes==null?"{}":fields.pagesizes})'),
	pageSize : 20 // 每页显示的记录数
};
/** 改变页的combo */
message.pageSizeCombo = new Share.pageSizeCombo({
			value : '20',
			listeners : {
				select : function(comboBox) {
					message.pageSize = parseInt(comboBox.getValue());
					message.bbar.pageSize = parseInt(comboBox.getValue());
					message.store.baseParams.limit = message.pageSize;
					message.store.baseParams.start = 0;
					message.store.load();
				}
			}
		});
// 覆盖已经设置的。具体设置以当前页面的pageSizeCombo为准
message.pageSize = parseInt(message.pageSizeCombo.getValue());
/** 基本信息-数据源 */
message.store = new Ext.data.Store({
			autoLoad : true,
			remoteSort : true,
			baseParams : {
				head  : '',
				start : 0,
				limit : message.pageSize
			},
			proxy : new Ext.data.HttpProxy({// 获取数据的方式
				method : 'POST',
				url : message.all
			}),
			reader : new Ext.data.JsonReader({// 数据读取器
				totalProperty : 'results', // 记录总数
				root : 'rows' // Json中的列表数据根节点
			}, ['id', 'head', 'body', 'begin_date', 'end_date','mxdx',
				'release_time','release_ip','release_per','isdisplay'
				]),
			listeners : {
				'load' : function(store, records, options) {
				
					message.alwaysFun();
				}
			}
		});
/** 基本信息-选择模式 */
/*message.selModel = new Ext.grid.CheckboxSelectionModel({
			singleSelect : false,
			locked: true,
			listeners : {
				'rowselect' : function(selectionModel, rowIndex, record) {
					message.deleteAction.enable();
					message.editAction.enable();
					message.viewAction.enable();
				},
				'rowdeselect' : function(selectionModel, rowIndex, record) {
					message.alwaysFun();
				}
			}
		});*/
message.selModel = new Ext.grid.AbstractSelectionModel({
	
});

/** 基本信息-数据列 */
message.colModel = new Ext.ux.grid.LockingColumnModel({
			defaults : {
				sortable : true,
				width : 140
			},
			columns : [message.selModel,new Ext.grid.RowNumberer({locked: true}), {
						hidden : true,
						header : '字段ID',
						dataIndex : 'id'
					},{
						hidden : true,
						header : '操作',
						dataIndex : '',
						width : 40,
						renderer : function(v) {
							return '<a href=javascript:message.viewAction.execute()><img src="${ctx}/resources/images/icons/tabs.gif" boder=0 vspace=0 hspace=0 title="阅读"/></a>';
						}
					}, {
						header : '标题',
						dataIndex : 'head',
						locked: true
					}, {
						hidden : true,
						header : '内容',
						dataIndex : 'body'
					}, {
						header : '开始时间',
						dataIndex : 'begin_date'
					}, {
						header : '结束时间',
						dataIndex : 'end_date'
					}, {
						header : '面向对象',
						dataIndex : 'mxdx'
					}, {
						header : '发布时间',
						dataIndex : 'release_time'
					}, {
						header : '发布IP',
						dataIndex : 'release_ip'
					},{
						header : '发布人',
						dataIndex : 'release_per'
					},{
						// (0:否;1:是)
						header : '是否显示',
						dataIndex : 'isdisplay',
						renderer : function(v) {
							return Share.map(v,message.isdisplay , '');
						}
					}]
		});
/** 新建 */
message.addAction = new Ext.Action({
			text : '新建',
			iconCls : 'field_add',
			handler : function() {
				message.addWindow.setIconClass('field_add'); // 设置窗口的样式
				message.addWindow.setTitle('新建记录'); // 设置窗口的名称
				message.addWindow.show().center(); // 显示窗口
				message.formPanel.getForm().reset(); // 清空表单里面的元素的值.
				message.isdisplayCombo.clearValue();
			}
		});
/** 编辑 */
message.editAction = new Ext.Action({
			text : '编辑',
			iconCls : 'field_edit',
			disabled : true,
			handler : function() {
				var record = message.grid.getSelectionModel().getSelected();
				message.addWindow.setIconClass('field_edit'); // 设置窗口的样式
				message.addWindow.setTitle('编辑记录'); // 设置窗口的名称
				message.addWindow.show().center();
				message.formPanel.getForm().reset();
				message.formPanel.getForm().loadRecord(record);
			}
		});
/** 浏览 */		
message.viewAction = new Ext.Action({
			text : '浏览',
			iconCls : 'tabs',
			disabled : true,
			handler : function() {
				var record = message.grid.getSelectionModel().getSelected();
				message.viewWindow.setIconClass('tabs'); // 设置窗口的样式
				message.viewWindow.setTitle('浏览信息'); // 设置窗口的名称
				message.viewWindow.show().center();
				message.viewformPanel.getForm().reset();
				message.viewformPanel.getForm().loadRecord(record);
				message.bodypanel.update('<br><div style="font-size:12px">'+record.get('body')+'</div>');
				message.headlabel.update('<center style="font-size:15px">'+record.get('head')+'</center>');
			}
		});		
/** 删除 */
message.deleteAction = new Ext.Action({
			text : '删除',
			iconCls : 'field_delete',
			disabled : true,
			handler : function() {
				message.delFun();
			}
		});
/** 查询 */
message.searchField = new Ext.ux.form.SearchField({
			store : message.store,
			paramName : 'head',
			emptyText : '请输入标题名称',
			style : 'margin-left: 5px;'
		});
/** 导出EXCEL */
message.xlsAction = new Ext.Action({
			text : '导出EXCEL',
			iconCls : 'xls',
			disabled : false,
			handler : function() {
				message.xlsFun();
			}
		});
/** 集成菜单区 */
message.menus=new Ext.menu.Menu({
	items:[  
     	message.xlsAction
    	]
    });  
message.menus.add('separator');  
message.menus_tar=new Ext.Toolbar({});  
message.menus_tar.add({
  	text:'选择操作',
   	menu:message.menus
 });

		
/** 提示 */
message.tips = '&nbsp;<font color="red"><b>提示信息</b></font>';
/** 顶部工具栏 */
message.tbar = [message.addAction, '-', message.editAction, '-',message.viewAction,'-',
		message.deleteAction, '-', message.searchField,'-',{iconCls:'system_settings',text:'选择操作',menu:message.menus},'-',message.tips];

/** 底部工具条 */
message.bbar = new Ext.PagingToolbar({
			pageSize : message.pageSize,
			store : message.store,
			displayInfo : true,
			// plugins : new Ext.ux.ProgressBarPager(), // 分页进度条
			items : ['-', '&nbsp;', message.pageSizeCombo]
		});
/** 基本信息-表格 */
message.grid = new Ext.grid.GridPanel({
			store : message.store,
			colModel : message.colModel,
			selModel : message.selModel,
			tbar : message.tbar,
			bbar : message.bbar,
			autoScroll : 'auto',
			region : 'center',
			loadMask : true,
			// autoExpandColumn :'fieldDesc',
			stripeRows : true,
			listeners : {},
			viewConfig : {},
			view: new Ext.ux.grid.LockingGridView()
		});

message.isdisplayCombo = new Ext.form.ComboBox({
			fieldLabel : '是否显示',
			hiddenName : 'isdisplay',
			name : 'isdisplay',
			triggerAction : 'all',
			mode : 'local',
			store : new Ext.data.ArrayStore({
						fields : ['v', 't'],
						data : Share.map2Ary(message.isdisplay)
					}),
			valueField : 'v',
			displayField : 't',
			allowBlank : false,
			editable : false,
			anchor : '99%'
		});
/** 基本信息-详细信息的form */
message.formPanel = new Ext.form.FormPanel({
			frame : false,
			title : '记录信息',
			bodyStyle : 'padding:10px;border:0px',
			labelwidth : 30,
		//	defaultType : 'textfield',
		//	frame : true,
			monitorValid:true,
			items : [{
						xtype : 'hidden',
						fieldLabel : 'ID',
						name : 'id',
						anchor : '99%'
					}, {
						xtype:'textfield',
						fieldLabel : '标题',
						maxLength : 100,
						allowBlank : false,
						name : 'head',
						anchor : '99%'
					},{
						xtype: 'ckeditor',
						fieldLabel : '内容',
						name : 'body',
						CKConfig: {height : 220},
						anchor : '99%'
					},{
						layout : 'column',
						region:'center',
						border : false,		
						fieldLabel : '&nbsp;&nbsp;其他设置',								
						items : [
							{columnWidth : .25,							 
							 layout:'form',
							 labelWidth : 60,
							 border : false,
							 items : [
							 	{
									xtype : 'datefield',
									format: 'Y-m-d H:i:s',
									fieldLabel : '开始时间',
									allowBlank : false,
									name : 'begin_date',
									value: new Date(),
									anchor : '99%'
								}
							 ]},
							 {columnWidth : .25,
							 layout:'form',
							 labelWidth : 60,
							 border : false,
							 items : [
							 	{
									xtype : 'datefield',
									format: 'Y-m-d H:i:s',
									fieldLabel : '结束时间',
									allowBlank : false,
									name : 'end_date',
									value: Date.parseDate(new Date().format('Y-m')+'-28 00:00:00','Y-m-d H:i:s'),
									anchor : '99%'
								}
							 ]},
							 {columnWidth : .3,
							 layout:'form',
							 labelWidth : 60,
							 border : false,
							 items : [
							 	{
									xtype:'textfield',
									fieldLabel : '面向对象',
									allowBlank : false,
									name : 'mxdx',
									anchor : '99%'
								}
							 ]},
							 {columnWidth : .2,
							 layout:'form',
							 labelWidth : 60,
							 border : false,
							 items : [
							 	message.isdisplayCombo
							 ]}
							 
							
						]}]
		});
/** 编辑新建窗口 */
message.addWindow = new Ext.Window({
			layout : 'fit',
			width : 900,
			height : 520,
			closeAction : 'hide',
			plain : true,
			modal : true,
			resizable : true,
			items : [message.formPanel],
			buttons : [{
						text : '保存',
						handler : function() {
							message.saveFun();
						}
					}, {
						text : '重置',
						handler : function() {
							var form = message.formPanel.getForm();
							var id = form.findField("id").getValue();
							form.reset();
							if (id != '')
								form.findField("id").setValue(id);
						}
					}]
		});
		
		
/** 消息查看窗口 */		
message.bodypanel = new Ext.Panel({
			title : '<center><font color=red>&nbsp;</font></center>',
			border: false,
			autoScroll :true,
			height : 410,
			width : 860,
			bodyStyle : 'border:0px',
			closable:false,
			html : ''
		});
message.headlabel = new Ext.form.Label({
			fieldLabel : '',						
			html : ''
		});
message.viewformPanel = new Ext.form.FormPanel({
			frame : false,
			title : '',
			bodyStyle : 'padding:10px;border:0px',
			labelwidth : 10,
		//	defaultType : 'textfield',
		//	frame : true,
		//	monitorValid:true,
			items : [message.headlabel,message.bodypanel]
		});	
message.viewWindow = new Ext.Window({
			layout : 'fit',
			width : 900,
			height : 520,
			closeAction : 'hide',
			plain : true,
			modal : true,
			resizable : true,
			items : [message.viewformPanel],
			buttons : [{
				text : '已读',
				handler : function() {
					message.viewWindow.hide();
				}
			}]
		});		
			
message.alwaysFun = function() {
	Share.resetGrid(message.grid);
	message.deleteAction.disable();
	message.editAction.disable();
	message.viewAction.disable();
	
};
message.saveFun = function() {
	var form = message.formPanel.getForm();
	if (!form.isValid()) {
		return;
	}
	// 发送请求
	Share.AjaxRequest({
				url : message.save,
				params : form.getValues(),
				callback : function(json) {
					message.addWindow.hide();
					message.alwaysFun();
					message.store.reload();
				}
			});
};
message.delFun = function() {
	var ids = [];	
    
    var sm = message.grid.getSelectionModel();
	var store = message.grid.getStore();
	var view = message.grid.getView();
	for (var i = 0; i < view.getRows().length; i++) {
		if (sm.isSelected(i)) {
			ids.push(store.getAt(i).get('id'));
		}
	}
	
//	var record = message.grid.getSelectionModel().getSelected(); 单条记录
	Ext.Msg.confirm('提示', '确定要删除选中的'+ids.length+'条记录吗?', function(btn, text) {
				if (btn == 'yes') {
					// 发送请求
					Share.AjaxRequest({
								url : message.del + ids,
								callback : function(json) {
									message.alwaysFun();
									message.store.reload();
								}
							});
				}
			});
};
message.xlsFun =function(){
	var head=message.store.baseParams['head'];
	/*var iframe = document.createElement("iframe");
	iframe.src= message.xls+"?head="+head;
	Ext.getBody().appendChild(iframe);*/
	//xls.location=message.xls+"?head="+head;
	window.open(message.xls+"?head="+head,target='xls');
};

message.myPanel = new Ext.Panel({
			id : '${param.id}' + '_panel',
			renderTo : '${param.id}',
			layout : 'border',
			boder : false,
			height : index.tabPanel.getInnerHeight() - 1,
			items : [message.grid]
		});